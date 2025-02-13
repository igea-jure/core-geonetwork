/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.repository;

import org.fao.geonet.domain.*;
import org.fao.geonet.utils.Log;
import org.springframework.data.jpa.domain.Specification;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.criteria.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implementation for all {@link User} queries that cannot be automatically generated by
 * Spring-data.
 *
 * @author Jesse
 */
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public User findOne(final String userId) {
        return entityManager.find(User.class, Integer.valueOf(userId));
    }

    @Override
    public User findOneByEmail(@Nonnull final String email) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);
        Join<User, String> joinedEmailAddresses = root.join(User_.emailAddresses);

        // Case in-sensitive email search
        query.where(cb.equal(cb.lower(joinedEmailAddresses), email.toLowerCase()));
        query.orderBy(cb.asc(root.get(User_.username)));
        final List<User> resultList = entityManager.createQuery(query).getResultList();
        if (resultList.isEmpty()) {
            return null;
        }
        if (resultList.size() > 1) {
            Log.error(Constants.DOMAIN_LOG_MODULE, String.format("The database is inconsistent.  There are multiple users with the email address: %s",
                email));
        }
        return resultList.get(0);
    }

    @Override
    public User findOneByEmailAndSecurityAuthTypeIsNullOrEmpty(@Nonnull final String email) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);
        Join<User, String> joinedEmailAddresses = root.join(User_.emailAddresses);

        final Path<String> authTypePath = root.get(User_.security).get(UserSecurity_.authType);
        query.where(cb.and(
            // Case in-sensitive email search
            cb.equal(cb.lower(joinedEmailAddresses), email.toLowerCase()),
            cb.or(cb.isNull(authTypePath), cb.equal(cb.trim(authTypePath), "")))
        ).orderBy(cb.asc(root.get(User_.username)));
        List<User> results = entityManager.createQuery(query).getResultList();


        if (results.isEmpty()) {
            return null;
        } else {
            if (results.size() > 1) {
                Log.error(Constants.DOMAIN_LOG_MODULE, String.format("The database is inconsistent.  There are multiple users with the email address: %s",
                    email));
            }
            return results.get(0);
        }
    }

    @Override
    public User findOneByUsernameAndSecurityAuthTypeIsNullOrEmpty(@Nonnull final String username) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);

        final Path<String> authTypePath = root.get(User_.security).get(UserSecurity_.authType);
        final Path<String> usernamePath = root.get(User_.username);
        // Case in-sensitive username search
        query.where(cb.and(
            cb.equal(cb.lower(usernamePath), username.toLowerCase()),
            cb.or(cb.isNull(authTypePath), cb.equal(cb.trim(authTypePath), "")))
        ).orderBy(cb.asc(root.get(User_.username)));
        List<User> results = entityManager.createQuery(query).getResultList();

        if (results.isEmpty()) {
            return null;
        } else {
            if (results.size() > 1) {
                Log.error(Constants.DOMAIN_LOG_MODULE, String.format("The database is inconsistent.  There are multiple users with username: %s",
                    username));
            }
            return results.get(0);
        }
    }

    @Nonnull
    @Override
    public List<String> findDuplicatedUsernamesCaseInsensitive() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);

        Root<User> userRoot = query.from(User.class);
        query = query.select(cb.lower(userRoot.get(User_.username)));
        query.groupBy(cb.lower(userRoot.get(User_.username)));
        query.having(cb.gt(cb.count(userRoot), 1));

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    @Nonnull
    public List<Pair<Integer, User>> findAllByGroupOwnerNameAndProfile(@Nonnull final Collection<Integer> metadataIds,
                                                                       @Nullable final Profile profile) {
        List<Pair<Integer, User>> results = new ArrayList<>();

        results.addAll(findAllByGroupOwnerNameAndProfileInternal(metadataIds, profile, false));
        results.addAll(findAllByGroupOwnerNameAndProfileInternal(metadataIds, profile, true));

        return results;
    }

    private List<Pair<Integer, User>> findAllByGroupOwnerNameAndProfileInternal(@Nonnull final Collection<Integer> metadataIds,
                                                                                @Nullable final Profile profile, boolean draft) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);

        Root<User> userRoot = query.from(User.class);
        Root<UserGroup> userGroupRoot = query.from(UserGroup.class);

        Predicate metadataPredicate;
        Predicate ownerPredicate;

        if (!draft) {
            Root<Metadata> metadataRoot = query.from(Metadata.class);
            query.multiselect(metadataRoot.get(Metadata_.id), userRoot);
            metadataPredicate = metadataRoot.get(Metadata_.id).in(metadataIds);

            ownerPredicate = cb.equal(metadataRoot.get(Metadata_.sourceInfo).get(MetadataSourceInfo_.groupOwner),
                userGroupRoot.get(UserGroup_.id).get(UserGroupId_.groupId));
        } else {
            Root<MetadataDraft> metadataRoot = query.from(MetadataDraft.class);
            query.multiselect(metadataRoot.get(MetadataDraft_.id), userRoot);
            metadataPredicate = metadataRoot.get(MetadataDraft_.id).in(metadataIds);

            ownerPredicate = cb.equal(metadataRoot.get(MetadataDraft_.sourceInfo).get(MetadataSourceInfo_.groupOwner),
                userGroupRoot.get(UserGroup_.id).get(UserGroupId_.groupId));
        }


        Predicate userToGroupPredicate = cb.equal(userGroupRoot.get(UserGroup_.id).get(UserGroupId_.userId), userRoot.get(User_.id));

        Predicate basePredicate = cb.and(metadataPredicate, ownerPredicate, userToGroupPredicate);
        if (profile != null) {
            Expression<Boolean> profilePredicate = cb.equal(userGroupRoot.get(UserGroup_.id).get(UserGroupId_.profile), profile);
            query.where(cb.and(basePredicate, profilePredicate));
        } else {
            query.where(basePredicate);
        }

        query.distinct(true);

        List<Pair<Integer, User>> results = new ArrayList<>();

        for (Tuple result : entityManager.createQuery(query).getResultList()) {
            Integer mdId = (Integer) result.get(0);
            User user = (User) result.get(1);
            results.add(Pair.read(mdId, user));
        }
        return results;
    }

    @Nonnull
    @Override
    public List<User> findAllUsersThatOwnMetadata() {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<User> query = cb.createQuery(User.class);

        final Root<Metadata> metadataRoot = query.from(Metadata.class);
        final Root<User> userRoot = query.from(User.class);

        query.select(userRoot);

        final Path<Integer> ownerPath = metadataRoot.get(Metadata_.sourceInfo).get(MetadataSourceInfo_.owner);
        Expression<Boolean> ownerExpression = cb.equal(ownerPath, userRoot.get(User_.id));
        query.where(ownerExpression);
        query.distinct(true);

        return entityManager.createQuery(query).getResultList();
    }

    @Nonnull
    @Override
    public List<User> findAllUsersInUserGroups(@Nonnull final Specification<UserGroup> userGroupSpec) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<User> query = cb.createQuery(User.class);

        final Root<UserGroup> userGroupRoot = query.from(UserGroup.class);
        final Root<User> userRoot = query.from(User.class);

        query.select(userRoot);

        final Path<Integer> ownerPath = userGroupRoot.get(UserGroup_.id).get(UserGroupId_.userId);
        Expression<Boolean> ownerExpression = cb.equal(ownerPath, userRoot.get(User_.id));
        query.where(cb.and(ownerExpression, userGroupSpec.toPredicate(userGroupRoot, query, cb)));
        query.distinct(true);

        return entityManager.createQuery(query).getResultList();
    }

}

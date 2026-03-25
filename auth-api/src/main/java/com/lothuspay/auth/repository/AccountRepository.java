package com.lothuspay.auth.repository;

import com.lothuspay.auth.model.accounts.Account;
import com.lothuspay.auth.model.accounts.role.AccountRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends MongoRepository<Account, String> {

    Account findByEmail(String email);
    Account findByPhone(String phone);

    Account findAccountByDocument_Number(String documentNumber);

    Page<Account> findByRolesContaining(AccountRole role, Pageable pageable);

    Page<Account> findByActive(Boolean active, Pageable pageable);

    @Query("{ $or: [ { 'email': { $regex: ?0, $options: 'i' } }, { 'firstName': { $regex: ?0, $options: 'i' } }, { 'lastName': { $regex: ?0, $options: 'i' } } ] }")
    Page<Account> findByEmailOrNameContainingIgnoreCase(String search, Pageable pageable);

    @Query("{ $and: [ { $or: [ { 'email': { $regex: ?0, $options: 'i' } }, { 'firstName': { $regex: ?0, $options: 'i' } }, { 'lastName': { $regex: ?0, $options: 'i' } } ] }, { 'roles': { $in: ?1 } } ] }")
    Page<Account> findByEmailOrNameContainingIgnoreCaseAndRolesIn(String search, List<AccountRole> roles, Pageable pageable);

    @Query("{ $and: [ { $or: [ { 'email': { $regex: ?0, $options: 'i' } }, { 'firstName': { $regex: ?0, $options: 'i' } }, { 'lastName': { $regex: ?0, $options: 'i' } } ] }, { 'active': ?1 } ] }")
    Page<Account> findByEmailOrNameContainingIgnoreCaseAndActive(String search, Boolean active, Pageable pageable);

    long countByActive(Boolean active);

    long countByRolesContaining(AccountRole role);

    @Query("{ 'document.submitted': true, 'document.verified': false }")
    Page<Account> findByDocumentSubmittedAndNotVerified(Pageable pageable);

    @Query("{ 'document.submitted': true, 'document.verified': true }")
    Page<Account> findByDocumentSubmittedAndVerified(Pageable pageable);

    @Query("{ 'document.submitted': true, 'document.verified': false, 'document.rejectionReason': { $exists: true, $ne: null } }")
    Page<Account> findByDocumentRejected(Pageable pageable);

    @Query("{ 'document.submitted': true }")
    Page<Account> findByDocumentSubmitted(Pageable pageable);

    long countByDocument_SubmittedAndDocument_Verified(boolean submitted, boolean verified);

    long countByDocument_Submitted(boolean submitted);

}

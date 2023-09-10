package com.mottakin.onlineBookLibraryApplication.repository;

import com.mottakin.onlineBookLibraryApplication.entity.BookEntity;
import com.mottakin.onlineBookLibraryApplication.entity.BorrowReturnEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository

public interface BorrowReturnRepository extends JpaRepository<BorrowReturnEntity, Long> {
    Optional<BorrowReturnEntity> findByBookEntity(Optional<BookEntity> bookEntity);
}

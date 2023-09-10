package com.mottakin.onlineBookLibraryApplication.service.impl;

import com.mottakin.onlineBookLibraryApplication.constants.AppConstants;
import com.mottakin.onlineBookLibraryApplication.entity.BookEntity;
import com.mottakin.onlineBookLibraryApplication.entity.BorrowReturnEntity;
import com.mottakin.onlineBookLibraryApplication.entity.UserEntity;
import com.mottakin.onlineBookLibraryApplication.exception.BookNotFoundException;
import com.mottakin.onlineBookLibraryApplication.model.UserDto;
import com.mottakin.onlineBookLibraryApplication.repository.BookRepository;
import com.mottakin.onlineBookLibraryApplication.repository.BorrowReturnRepository;
import com.mottakin.onlineBookLibraryApplication.repository.UserRepository;
import com.mottakin.onlineBookLibraryApplication.service.UserService;
import com.mottakin.onlineBookLibraryApplication.utils.JWTUtils;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional

public class UserServiceImpl implements UserService, UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private BorrowReturnRepository borrowReturnRepository;

    public BookEntity createBook(BookEntity book) {
        return bookRepository.save(book);
    }
    public BookEntity updateBook(BookEntity updatedBook) {
        Optional<BookEntity> existingBook = bookRepository.findById(updatedBook.getId());
        if (existingBook.isPresent()) {
            BookEntity bookToUpdate = existingBook.get();
            bookToUpdate.setTitle(updatedBook.getTitle());
            return bookRepository.save(bookToUpdate);
        } else {
            throw new BookNotFoundException("Book not found with ID: " + updatedBook.getId());
        }
    }
    public void deleteBook(Long bookId) {
        Optional<BookEntity> existingBook = bookRepository.findById(bookId);
        if (existingBook.isPresent()) {
            bookRepository.deleteById(bookId);
        } else {
            throw new BookNotFoundException("Book not found with ID: " + bookId);
        }
    }
    public List<BookEntity> getAllBooks() {
        return bookRepository.findAll();
    }
    @Override
    public UserDto createUser(UserDto user) throws Exception {
        if(userRepository.findByEmail(user.getEmail()).isPresent())
            throw new Exception("Record already exists");

        ModelMapper modelMapper = new ModelMapper();
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(user.getEmail());
        userEntity.setFirstName(user.getFirstName());
        userEntity.setLastName(user.getLastName());
        userEntity.setAddress(user.getAddress());
        userEntity.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        String publicUserId = JWTUtils.generateUserID(10);
        userEntity.setUserId(publicUserId);
        userEntity.setRole(user.getRole());
        UserEntity storedUserDetails = userRepository.save(userEntity);
        UserDto returnedValue = modelMapper.map(storedUserDetails,UserDto.class);
        String accessToken = JWTUtils.generateToken(userEntity.getEmail());
        returnedValue.setAccessToken(AppConstants.TOKEN_PREFIX + accessToken);
        return returnedValue;
    }
    @Override
    public UserDto getUser(String email) {
        UserEntity userEntity = userRepository.findByEmail(email).get();
        if(userEntity == null) throw new UsernameNotFoundException("No record found");
        UserDto returnValue = new UserDto();
        BeanUtils.copyProperties(userEntity,returnValue);
        return returnValue;
    }

    @Override
    public UserDto getUserByUserId(String userId) throws Exception {
        UserDto returnValue = new UserDto();
        UserEntity userEntity = userRepository.findByUserId(userId).orElseThrow(Exception::new);
        BeanUtils.copyProperties(userEntity,returnValue);
        return returnValue;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByEmail(email).get();
        if(userEntity==null) throw new UsernameNotFoundException(email);
        return new User(userEntity.getEmail(),userEntity.getPassword(),
                true,true,true,true,new ArrayList<>());
    }

    /*Book Borrow and Return Service*/
    @Override
    public boolean borrowBook(Long bookId) {
        Optional<BookEntity> optionalBook = bookRepository.findById(bookId);

        if (optionalBook.isPresent()) {
            BookEntity book = optionalBook.get();

            if (!book.isAvailable()) {
                return false; // Book is already borrowed
            }

            Optional<BookEntity> bookEntity = bookRepository.findById(bookId);
            Optional<BorrowReturnEntity> optionalBorrowRecord = borrowReturnRepository.findByBookEntity(bookEntity);

            if (optionalBorrowRecord.isPresent()) {
                BorrowReturnEntity borrowRecord = optionalBorrowRecord.get();
                if (!borrowRecord.isAvailability()) {
                    // Check due date to see if it's overdue
                    LocalDate dueDate = borrowRecord.getGetDueDate();
                    LocalDate currentDate = LocalDate.now();
                    if (currentDate.isAfter(dueDate)) {
                        return false; // Book is not available due to overdue return
                    }
                }
            }

            // Borrow the book
            BorrowReturnEntity bookBorrow = new BorrowReturnEntity();
            bookBorrow.setBookEntity(book);
            bookBorrow.setAvailability(false);
            // Set a due date (e.g., 30 days from now)
            LocalDate dueDate = LocalDate.now().plusDays(30);
            bookBorrow.setGetDueDate(dueDate);
            borrowReturnRepository.save(bookBorrow);
            return true;
        }

        return false; // Book not found
    }
    @Override
    public boolean returnBook(Long bookId) {
        Optional<BookEntity> bookEntity = bookRepository.findById(bookId);
        Optional<BorrowReturnEntity> optionalBorrowRecord = borrowReturnRepository.findByBookEntity(bookEntity);

        if (optionalBorrowRecord.isPresent()) {
            BorrowReturnEntity borrowRecord = optionalBorrowRecord.get();

            if (borrowRecord.isAvailability()) {
                return false; // Book is not borrowed
            }

            // Check due date to see if it's overdue
            LocalDate dueDate = borrowRecord.getGetDueDate();
            LocalDate currentDate = LocalDate.now();

            if (currentDate.isAfter(dueDate)) {
                return false; // Book return is overdue
            }

            // Return the book
            borrowRecord.setAvailability(true);
            borrowRecord.setGetDueDate(null);
            borrowReturnRepository.save(borrowRecord);
            return true;
        }

        return false; // Book borrow record not found
    }



}

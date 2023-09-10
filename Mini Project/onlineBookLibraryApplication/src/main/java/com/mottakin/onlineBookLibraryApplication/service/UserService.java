package com.mottakin.onlineBookLibraryApplication.service;

import com.mottakin.onlineBookLibraryApplication.model.UserDto;

public interface UserService {
    UserDto createUser(UserDto user) throws Exception;
    UserDto getUser(String email);

    UserDto getUserByUserId(String id) throws Exception;

    /*Book Borrow and Return Service*/
    boolean borrowBook(Long bookId);

    boolean returnBook(Long bookId);
}

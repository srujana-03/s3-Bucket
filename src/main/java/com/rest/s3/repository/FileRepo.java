package com.rest.s3.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.util.Streamable;

import com.rest.s3.model.FileData;
import com.rest.s3.model.UserData;

public interface FileRepo extends JpaRepository<FileData, Long> {

	Optional<FileData> findByFileName(String filename);

	Optional<FileData> findByFileNameAndUserId(String filename, Long userId);

	List<FileData> findByFileNameStartingWith(String fileName);

	Page<FileData> findByUserId(Long userId, Pageable pageable);

	

}

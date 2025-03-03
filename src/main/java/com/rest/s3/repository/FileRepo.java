package com.rest.s3.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rest.s3.model.FileData;

public interface FileRepo extends JpaRepository<FileData, Long> {

	Optional<FileData> findByFileName(String filename);

}

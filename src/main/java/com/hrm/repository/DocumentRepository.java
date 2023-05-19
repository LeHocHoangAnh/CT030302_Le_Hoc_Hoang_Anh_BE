package com.hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hrm.entity.Document;
import com.hrm.model.response.employee.DocumentListResponse;

import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Integer>{

	@Query(value="SELECT "
			+ "		doc.id as id, "
			+ "		doc.name as name, "
			+ "		doc.description as description, "
			+ "		to_char(doc.updated_at, 'DD Mon YYYY') as updatedAt, "
			+ "		pro.full_name as updatedBy "
			+ "	  FROM document AS doc"
			+ "	  JOIN profile AS pro ON doc.updated_by = pro.employee_id", nativeQuery = true)
	List<DocumentListResponse> getDocumentListDisplay();
	
	@Query(value="SELECT "
			+ "		doc.id as id, "
			+ "		doc.name as name, "
			+ "		doc.description as description, "
			+ "		to_char(doc.updated_at, 'DD Mon YYYY') as updatedAt, "
			+ "		pro.full_name as updatedBy "
			+ "	  FROM document AS doc"
			+ "	  JOIN profile AS pro ON doc.updated_by = pro.employee_id"
			+ "	  WHERE delete_flag = false ORDER BY doc.updated_at DESC LIMIT 1 ", nativeQuery = true)
	DocumentListResponse findDocumnetPinnedToHeader();
}

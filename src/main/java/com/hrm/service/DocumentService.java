package com.hrm.service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

import com.hrm.common.Constants;
import com.hrm.entity.Document;
import com.hrm.exception.RecordNotFoundException;
import com.hrm.model.ApiResponse;
import com.hrm.model.request.hr.CreateOrEditDocumentRequest;
import com.hrm.model.response.employee.DocumentListResponse;
import com.hrm.repository.DocumentRepository;

@Service
@Transactional
public class DocumentService {
    @Autowired
    private DocumentRepository documentRepository;

	public ApiResponse getDocumentList() {
		List<DocumentListResponse> response = documentRepository.getDocumentListDisplay();
		if(response==null || response.size()<=0) {
			throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
		}
		return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, response);
	}

	public ApiResponse getDetailDocumentById(String id) {
		if(id.equals("null")) {
			return new ApiResponse(Constants.HTTP_CODE_500, "ID rỗng", null);
		}
		int intId = Integer.parseInt(id);
		Optional<Document> documentOptional = documentRepository.findById(intId);
		if(documentOptional.isEmpty() || documentOptional.get()==null) {
			throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
		}

		return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, documentOptional.get());
	}

    public ApiResponse editDocument(CreateOrEditDocumentRequest request) {
    	byte[] byteaHTML = new byte[0];
    	Document document;
    	if(StringUtils.isNotBlank(request.getContent())) {
    		byteaHTML = request.getContent().getBytes(StandardCharsets.UTF_8);
    	}
    	if(request.getId()==null) {
    		document = new Document();
    		document.setDeleteFlag(false);
    		document.setCommonRegister();
    	}
    	else {
    		Optional<Document> documentOptional = documentRepository.findById(request.getId());
    		if(!documentOptional.isPresent() || documentOptional.get()==null) {
    			throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
    		}
    		document = documentOptional.get();
    		document.setCommonUpdate();
    	}
		document.setName(request.getName());
		document.setDescription(request.getDescription());
		document.setContent(byteaHTML);
    	
    	documentRepository.save(document);
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, null);
    }

	public ApiResponse deleteDocument(Integer id) {
		Optional<Document> documentOpt = documentRepository.findById(id);
		if(!documentOpt.isPresent() || documentOpt.get()==null) {
			throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
		}
		documentRepository.delete(documentOpt.get());
		return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, null);
	}

	public ApiResponse getDocumentToHeader() {
		return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, documentRepository.findDocumnetPinnedToHeader());
	}
}

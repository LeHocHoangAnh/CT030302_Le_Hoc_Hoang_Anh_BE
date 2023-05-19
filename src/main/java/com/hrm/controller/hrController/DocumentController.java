package com.hrm.controller.hrController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hrm.model.ApiResponse;
import com.hrm.model.request.PaginationRequest;
import com.hrm.model.request.hr.CreateOrEditDocumentRequest;
import com.hrm.model.request.hr.CreateOrEditHistoryRequest;
import com.hrm.service.DocumentService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/api/hr/document")
@Api(description = "Chỉnh sửa tài liệu")
public class DocumentController {
    
    @Autowired
    DocumentService documentService;
    
    @ApiOperation(value = "Chỉnh sửa tài liệu nội bộ")
    @PostMapping("/edit")
    public ResponseEntity<ApiResponse> getListDocument(@RequestBody CreateOrEditDocumentRequest request) {
        return ResponseEntity.ok(documentService.editDocument(request));
    }
    
    @ApiOperation(value = "Xóa tài liệu nội bộ")
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse> deleteDocument(@RequestParam Integer id) {
        return ResponseEntity.ok(documentService.deleteDocument(id));
    }
}

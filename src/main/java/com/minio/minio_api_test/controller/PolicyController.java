package com.minio.minio_api_test.controller;

import com.minio.minio_api_test.service.MinioPolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/policy")
@RequiredArgsConstructor
@Slf4j
public class PolicyController {

    private final MinioPolicyService minioPolicyService;

    @PostMapping("/set-policy")
    public String setPolicy(@RequestParam String bucketName, @RequestBody String policyJson) {
        log.info("/api/policy/set-policy POST - {}", bucketName);

        try {
            minioPolicyService.setBucketPolicy(bucketName, policyJson);
            return "Policy successfully set for bucket: " + bucketName;
        } catch (Exception e) {
            return "Error setting policy: " + e.getMessage();
        }
    }

    @GetMapping("/get-policy")
    public String getPolicy(@RequestParam String bucketName) {
        log.info("/api/policy/get-policy GET - {}", bucketName);

        try {
            String policyJson = minioPolicyService.getBucketPolicy(bucketName);
            return "Policy for bucket " + bucketName + ": " + policyJson;
        } catch (Exception e) {
            return "Error retrieving policy: " + e.getMessage();
        }
    }

}

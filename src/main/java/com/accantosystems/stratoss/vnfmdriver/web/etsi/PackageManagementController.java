package com.accantosystems.stratoss.vnfmdriver.web.etsi;

import java.util.ArrayList;
import java.util.List;

import org.etsi.sol003.packagemanagement.VnfPkgInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.accantosystems.stratoss.vnfmdriver.service.ContentRangeNotSatisfiableException;
import com.accantosystems.stratoss.vnfmdriver.service.PackageManagementService;
import com.accantosystems.stratoss.vnfmdriver.service.PackageStateConflictException;

import io.swagger.annotations.ApiOperation;

@RestController("PackageManagementController")
@RequestMapping("/vnfpkgm/v1/vnf_packages")
public class PackageManagementController {

    private final static Logger logger = LoggerFactory.getLogger(GrantController.class);

    private static final String CONTENT_TYPE_APPLICATION_YAML = "application/yaml";
    private static final String CONTENT_TYPE_APPLICATION_ZIP = "application/zip";

    private final PackageManagementService packageManagementService;

    @Autowired
    public PackageManagementController(PackageManagementService packageManagementService) {
        this.packageManagementService = packageManagementService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Query VNF packages information.", notes = "Queries the information of the VNF packages matching the filter.")
    public ResponseEntity<List<VnfPkgInfo>> queryVnfPackages(@RequestParam(value = "filter", required = false) String filter,
                                                             @RequestParam(value = "all_fields", required = false) String allFields,
                                                             @RequestParam(value = "fields", required = false) String fields,
                                                             @RequestParam(value = "exclude_fields", required = false) String excludeFields,
                                                             @RequestParam(value = "exclude_default", required = false) String excludeDefault,
                                                             @RequestParam(value = "nextpage_opaque_marker", required = false) String nextPageOpaqueMarker) throws NotImplementedException {
        logger.info("Received VNF Package Query.");
        // This API is not yet implemented
        throw new NotImplementedException("Query VNF Packages Info API not yet implemented.");

    }

    @GetMapping(path = "/{vnfPkgId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Reads the information of an individual VNF package", notes = "Queries the information of the VNF packages matching the filter.")
    public ResponseEntity<VnfPkgInfo> getVnfPackage(@PathVariable String vnfPkgId) throws NotImplementedException {

        logger.info("Received Individual VNF package Info Get request.");

        VnfPkgInfo vnfInfo = packageManagementService.getVnfPackageInfo(vnfPkgId);
        return ResponseEntity.ok(vnfInfo);

    }

    @GetMapping(path = "/{vnfPkgId}/vnfd", produces = { MediaType.TEXT_PLAIN_VALUE, CONTENT_TYPE_APPLICATION_ZIP })
    @ApiOperation(value = "Reads the content of the VNFD within a VNF package.", notes = "This resource represents the VNFD contained in an on-boarded VNF package. The client can use this resource to obtain the content of the VNFD.")
    public ResponseEntity<?> getVnfd(@RequestHeader("Accept") List<String> acceptHeader, @PathVariable String vnfPkgId) throws ResponseTypeNotAcceptableException {

        logger.info("Received VNFD Get request for package id [{}]", vnfPkgId);

        boolean acceptsZip;
        if (acceptHeader.isEmpty()) {
            throw new ResponseTypeNotAcceptableException("No response type specified in Accept HTTP header. ");
        } else {
            List<String> acceptTypes = new ArrayList<String>(acceptHeader);
            acceptsZip = acceptTypes.remove(CONTENT_TYPE_APPLICATION_ZIP);
            acceptTypes.remove(MediaType.TEXT_PLAIN_VALUE);
            if (!acceptTypes.isEmpty()) {
                throw new ResponseTypeNotAcceptableException(String.format("Response type(s) not acceptable in Accept HTTP header: [%s]", String.join(",", acceptTypes)));
            }
        }

        if (acceptsZip) {
            // if application/zip is accepted (even if text/plain is also accepted) then return all as zip
            Resource zipResource = packageManagementService.getVnfdAsZip(vnfPkgId);
            HttpHeaders headers = new HttpHeaders();
            // TODO set content length: headers.setContentLength(?);
            headers.setContentType(MediaType.parseMediaType(CONTENT_TYPE_APPLICATION_ZIP));
            return new ResponseEntity<Resource>(zipResource, headers, HttpStatus.OK);
        } else {
            // only text/plain is accepted. Return as YAML only
            String vnfd = packageManagementService.getVnfdAsYaml(vnfPkgId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(CONTENT_TYPE_APPLICATION_YAML));
            return new ResponseEntity<String>(vnfd, headers, HttpStatus.OK);
        }

    }

    @GetMapping(path = "/{vnfPkgId}/package_content", produces = { CONTENT_TYPE_APPLICATION_ZIP })
    @ApiOperation(value = "Reads the content of a VNF package identified by the VNF package identifier allocated by the NFVO.", notes = "This resource represents a VNF package identified by the VNF package identifier allocated by the NFVO. The client can use this resource to fetch the content of the VNF package.")
    public ResponseEntity<Resource> getVnfPackageContent(@RequestHeader(value = "Content-Range", required = false) String contentRange,
                                                         @PathVariable String vnfPkgId) throws PackageStateConflictException,
                                                                                        ContentRangeNotSatisfiableException {

        logger.info("Received VNF Package Content Get request for package id [{}] and content range []", vnfPkgId, contentRange);

        Resource vnfPackage = packageManagementService.getVnfPackageContent(vnfPkgId, contentRange);
        HttpStatus responseStatus = contentRange != null ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(CONTENT_TYPE_APPLICATION_ZIP));
        return new ResponseEntity<Resource>(vnfPackage, headers, responseStatus);

    }

    @GetMapping(path = "/{vnfPkgId}/artifacts/{artifactPath}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ApiOperation(value = "Reads the content content of an artifact within a VNF package.", notes = "This resource represents an individual artifact contained in a VNF package. The client can use this resource to fetch the content of the artifact.")
    public ResponseEntity<Resource> getVnfPackageArtifact(@RequestHeader(value = "Content-Range", required = false) String contentRange, @PathVariable String vnfPkgId,
                                                          @PathVariable String artifactPath) throws PackageStateConflictException, ContentRangeNotSatisfiableException {

        logger.info("Received VNF Package Artifact Get request for package id [{}], artifact path [] and content range []", vnfPkgId, artifactPath, contentRange);

        Resource vnfPackageArtifact = packageManagementService.getVnfPackageArtifact(vnfPkgId, artifactPath, contentRange);
        HttpStatus responseStatus = contentRange != null ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM); // TODO - can we determine the actual type from the content
        return new ResponseEntity<Resource>(vnfPackageArtifact, headers, responseStatus);

    }

}
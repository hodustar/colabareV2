package cot.colabare.meetingboard.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import cot.colabare.meetingboard.domain.MeetingBoardAttachFileDTO;
import lombok.extern.log4j.Log4j;
import net.coobird.thumbnailator.Thumbnailator;

@Controller
@Log4j
public class MeetingBoardUploadController {

	private boolean checkImageType(File file){
		try {
			String contentType = Files.probeContentType(file.toPath());
			
			return contentType.startsWith("image");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	private String getFolder(){
	      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	      Date date = new Date();
	      String str = sdf.format(date);
	      return str.replace("-", File.separator);
	   }
	
	
	   @PostMapping(value = "/uploadMeetingBoard", produces= MediaType.APPLICATION_JSON_UTF8_VALUE)
	   @ResponseBody
	   public ResponseEntity<List<MeetingBoardAttachFileDTO>> uploadMeetingBoard(MultipartFile[] uploadFile) {

		  List<MeetingBoardAttachFileDTO> list = new ArrayList<MeetingBoardAttachFileDTO>();
		   
	      String uploadFolder = "c:\\upload";
	      String uploadFolderPath = getFolder();
	      
	      //make folder
	      File uploadPath = new File(uploadFolder, uploadFolderPath);
	      log.info("upload path: " + uploadPath);
	      
	      if(uploadPath.exists() == false){
	         uploadPath.mkdirs();
	      }
	      
	      // make yyyy/MM/dd folder

	      for (MultipartFile multipartFile : uploadFile) {
	    	  
	    	  MeetingBoardAttachFileDTO attachDTO = new MeetingBoardAttachFileDTO();
	    	  
	         String uploadFileName = multipartFile.getOriginalFilename();	// 파일 원본 이름
	         
	         uploadFileName = uploadFileName.substring(uploadFileName.lastIndexOf("\\")+1);
	         log.info("only file name :" + uploadFileName);
	         attachDTO.setFileName(uploadFileName);
	         
	         UUID uuid = UUID.randomUUID();
	         
	         uploadFileName = uuid.toString() + "_" + uploadFileName;
	         
	        

	         try {
	        	 File saveFile = new File(uploadPath, uploadFileName);
	        	 
	        	 multipartFile.transferTo(saveFile);
	        	 
	        	 attachDTO.setUuid(uuid.toString());
	        	 attachDTO.setUploadPath(uploadFolderPath);
	        	 
	        	 // check image type file
	        	 if(checkImageType(saveFile)){
	        		 
	        		 attachDTO.setImage(true);
	        		 
	        		 FileOutputStream thumbnail = new FileOutputStream(new File(uploadPath, "s_" + uploadFileName));
	        		 
	        		 Thumbnailator.createThumbnail(multipartFile.getInputStream(), thumbnail, 100, 100);
	        		 
	        		 thumbnail.close();
	        	 }
	        	 
	        	 list.add(attachDTO);
	        	 
	         } catch (Exception e) {
	            log.error(e.getMessage());
	         }

	      }
	      
	      return new ResponseEntity<>(list, HttpStatus.OK);
	   }
	
	@GetMapping("/displayMeetingBoard")
    @ResponseBody
    public ResponseEntity<byte[]> getFile(String fileName){
	    
	    log.info("fileName : " + fileName);
	    File file = new File("c:\\upload\\" + fileName);
	    
	    log.info("file : " + file);
	    
	    ResponseEntity<byte[]> result = null;
	    
	    try {
		 HttpHeaders header = new HttpHeaders();
		 
		 header.add("Content-Type", Files.probeContentType(file.toPath()));
		 result = new ResponseEntity<>(FileCopyUtils.copyToByteArray(file), header, HttpStatus.OK);
		 
	 } catch (Exception e) {
	 	 e.printStackTrace();
	 }
	    return result;
   }
    
    // 다운로드 이름 지정 메소드
    @GetMapping(value="/downloadMeetingBoard", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public ResponseEntity<Resource> downloadFileMeetingBoard( @RequestHeader("User-Agent") String userAgent, String fileName){
 	   
 	   log.info("download file : " + fileName);
 	   
 	   Resource resource = new FileSystemResource("c:\\upload\\" + fileName);
 	   
 	   log.info("resource : " + resource );
 	   
 	   if(resource.exists() == false){
 		   
 		   return new ResponseEntity<>(HttpStatus.NOT_FOUND);
 	   }
 	   
 	   String resourceName = resource.getFilename(); 	// 전체 경로
 	   
 	   // remove UUID
 	   String resourceOriginalName = resourceName.substring(resourceName.indexOf("_") + 1);
 	   
 	   HttpHeaders headers = new HttpHeaders();
 	   
 	   try {
 		   
 		   String downloadName = null;
 		   
 		   if(userAgent.contains("Trident")){
 			   
 			   log.info("IE brower");
 			   downloadName = URLEncoder.encode(resourceOriginalName, "UTF-8").replaceAll("\\+", " "); 
 		   
 		   }else if(userAgent.contains("Edge")){
 			
 			   log.info("Edge browser");
 			   downloadName = URLEncoder.encode(resourceOriginalName, "UTF-8").replaceAll("\\+", " "); 
 			   
 		   }else{
 			   
 			   log.info("Chrome browser");
 			   downloadName = new String(resourceOriginalName.getBytes("UTF-8"), "ISO-8859-1");
 			}
 		   
 		   log.info("downloadName : " + downloadName);

 			headers.add("Content-Disposition", "attachment; filename=" + downloadName);

 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}

 		return new ResponseEntity<Resource>(resource, headers, HttpStatus.OK);
 	}
	
	
	
	
	
	
}

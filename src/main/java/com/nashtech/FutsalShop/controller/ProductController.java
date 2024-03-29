package com.nashtech.FutsalShop.controller;

import javax.mail.Multipart;
import javax.servlet.http.HttpServletRequest;

import com.nashtech.FutsalShop.services.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nashtech.FutsalShop.DTO.ProductDTO;
import com.nashtech.FutsalShop.Utils.StringUtils;
import com.nashtech.FutsalShop.model.Product;
import com.nashtech.FutsalShop.exception.ObjectAlreadyExistException;
import com.nashtech.FutsalShop.exception.ObjectNotFoundException;
import com.nashtech.FutsalShop.security.JWT.JwtUtils;
import com.nashtech.FutsalShop.services.PersonService;
import com.nashtech.FutsalShop.services.ProductService;
import com.nashtech.FutsalShop.security.JWT.JwtAuthTokenFilter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1")
public class ProductController {
	@Autowired
	private ProductService productService;
	
	@Autowired
	private PersonService personService;

	@Autowired
	CloudinaryService cloudinaryService;

	@Autowired
	private JwtUtils jwtUtils;

	@GetMapping("/product/search/{id}")
	//@PreAuthorize("hasRole('USER') or hasRole('STAFF') or hasRole('ADMIN')")
	public Product getProduct(@PathVariable(name = "id") String id) {
		return productService.getProductInludeDeleted(id)
				.orElseThrow(() -> new ObjectNotFoundException("Could not find product with Id: " + id));
	}
	
	@Operation(summary = "Create a Product")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "The request has succeeded", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = Product.class)) }),
			@ApiResponse(responseCode = "303", description = "Error: Have an existed product ", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized, Need to login first!", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request: Invalid syntax", content = @Content),
			@ApiResponse(responseCode = "403", description = "Forbidden: Only ADMIN and STAFF can create Product", content = @Content),
			@ApiResponse(responseCode = "404", description = "Can not find the requested resource", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content) })

	@PostMapping("/product")

	//@PreAuthorize("hasAuthority('STAFF') or hasAuthority('ADMIN')")
	public Product saveProduct(HttpServletRequest request,
		   	@RequestParam String id,
			@RequestParam String name,
			@RequestParam float price,
			@RequestParam int quantity,
			@RequestParam int categoriesId,
			@RequestParam String description,
			@RequestParam String brand,
			@RequestParam String nameEmployeeUpdate,
			@RequestParam MultipartFile photo ) {
		//ben fe la ong fix cai formdata truyen dung du lieu la dzo day
		String jwt = JwtAuthTokenFilter.parseJwt(request);
		String username = jwtUtils.getUserNameFromJwtToken(jwt);
		//khuc nay la upload anh nay, neu không loi thi nó sẽ trả ra cho ông cái đường dẫn của ảnh
		String urlImage = cloudinaryService.upload(photo);
		ProductDTO newProduct = new ProductDTO(
				id, name, price, quantity, categoriesId, description,
				brand, LocalDateTime.now(), LocalDateTime.now(), nameEmployeeUpdate, urlImage
		);
//		System.out.println(123);
		//rồi trong cái tầng service này ông xử lý theo logic của ông r lưu xuống db
		return productService.createProduct(newProduct, Integer.parseInt(username));
	}

	@GetMapping("/product/checkExistId/{id}")
	public boolean checkExistId(@PathVariable(name = "id") String id) {
		return productService.checkExistId(id);
	}

	@GetMapping("/product/checkExistName/{name}")
	public boolean checkExistName(@PathVariable(name = "name") String name) {
		return productService.checkExistName(name);
	}

	@GetMapping("/product/checkExistNameUpdate")
	public boolean checkExistName(@RequestParam(name = "name") String name, @RequestParam(name = "id") String id) {
		return productService.checkExistNameUpdate(id, name);
	}

	@Operation(summary = "Delete a Product by id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "The request has succeeded", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = Product.class)) }),
			@ApiResponse(responseCode = "401", description = "Unauthorized, Need to login first!", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request: Invalid syntax", content = @Content),
			@ApiResponse(responseCode = "404", description = "Can not find the requested resource", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content) })
	@DeleteMapping("/product/{id}")
	@PreAuthorize("hasAuthority('STAFF') or hasAuthority('ADMIN')")
	public String deleteProduct(HttpServletRequest request, @PathVariable(name = "id") String id) {
		try {
			String jwt = JwtAuthTokenFilter.parseJwt(request);
			String userId = jwtUtils.getUserNameFromJwtToken(jwt);
			return productService.deleteProduct(id, Integer.parseInt(userId)) ? StringUtils.TRUE : StringUtils.FALSE;
		} catch (DataIntegrityViolationException | EmptyResultDataAccessException ex) {
			return StringUtils.FALSE;
		}
	}

	@Operation(summary = "Update a Product")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "The request has succeeded", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = Product.class)) }),
			@ApiResponse(responseCode = "401", description = "Unauthorized, Need to login first!", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request: Invalid syntax", content = @Content),
			@ApiResponse(responseCode = "404", description = "Can not find the requested resource", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content) })
	@PutMapping("/product/{id}")
	@PreAuthorize("hasAuthority('STAFF') or hasAuthority('ADMIN')")
	public String editProduct(HttpServletRequest request, @RequestBody ProductDTO product,
			@PathVariable(name = "id") String id) {
		String jwt = JwtAuthTokenFilter.parseJwt(request);
		String userId = jwtUtils.getUserNameFromJwtToken(jwt);
		 try {
		return productService.updateProduct(product, Integer.parseInt(userId)) ? StringUtils.TRUE : StringUtils.FALSE;
		} catch (ObjectAlreadyExistException ex) {
			return StringUtils.FALSE;
		}
	}


}

package com.nashtech.MyBikeShop.controller;

import java.util.NoSuchElementException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
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

import com.nashtech.MyBikeShop.DTO.CategoriesDTO;
import com.nashtech.MyBikeShop.Utils.StringUtils;
import com.nashtech.MyBikeShop.model.categories;
import com.nashtech.MyBikeShop.model.person;
import com.nashtech.MyBikeShop.exception.ObjectAlreadyExistException;
import com.nashtech.MyBikeShop.exception.ObjectNotFoundException;
import com.nashtech.MyBikeShop.exception.ObjectViolateForeignKeyException;
import com.nashtech.MyBikeShop.payload.response.MessageResponse;
import com.nashtech.MyBikeShop.security.JWT.JwtAuthTokenFilter;
import com.nashtech.MyBikeShop.security.JWT.JwtUtils;
import com.nashtech.MyBikeShop.services.CategoriesService;
import com.nashtech.MyBikeShop.services.PersonService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1")
public class CategoriesController {
	@Autowired
	CategoriesService cateService;

	@Autowired
	PersonService personService;

	@Autowired
	private JwtUtils jwtUtils;

	private static final Logger logger = Logger.getLogger(CategoriesController.class);

	@Operation(summary = "Get Categories by ID")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "The request has succeeded", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = categories.class)) }),
			@ApiResponse(responseCode = "401", description = "Unauthorized, Need to login first!", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request: Invalid syntax", content = @Content),
			@ApiResponse(responseCode = "404", description = "Can not find the requested resource", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content) })
	@GetMapping("/categories/{id}")
	@PreAuthorize("hasRole('USER') or hasRole('STAFF') or  hasRole('ADMIN')")
	public categories findCategories(@PathVariable(name = "id") int id) {
		return cateService.getCategories(id)
				.orElseThrow(() -> new ObjectNotFoundException("Could not find categories with Id: " + id));
	}

	@GetMapping("/categories/checkName")
	@PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
	public boolean checkExistCategoriesByName(@RequestParam(name = "name") String name,
			@RequestParam(name = "id") int id) {
		return cateService.checkExistName(id, name);
	}

	@Operation(summary = "Create/Update Categories")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "The request has succeeded", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = categories.class)) }),
			@ApiResponse(responseCode = "401", description = "Unauthorized, Need to login first!", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request: Invalid syntax", content = @Content),
			@ApiResponse(responseCode = "404", description = "Can not find the requested resource", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content) })
	@PostMapping("/categories")
	@PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
	public String createCategories(HttpServletRequest request, @RequestBody CategoriesDTO newCate) {
		String jwt = JwtAuthTokenFilter.parseJwt(request);
		String userId = jwtUtils.getUserNameFromJwtToken(jwt);
		try {
			person person = personService.getPerson(Integer.parseInt(userId)).get();
			boolean check = cateService.createCategories(newCate);
			if (check)
				logger.info("Account id " + userId + " created category id " + newCate.getId() + " success");
			else
				logger.error("Account id " + userId + " created category id " + newCate.getId() + " failed");
			return check ? StringUtils.TRUE : StringUtils.FALSE;
		} catch (IllegalArgumentException | ConstraintViolationException | ObjectAlreadyExistException ex) {
			logger.error(ex.getMessage());
			return StringUtils.FALSE;
		} catch (NoSuchElementException ex) {
			logger.error("Account id " + userId + " create created id " + newCate.getId()
					+ " failed: No found account with ID " + userId);
			throw new ObjectNotFoundException("Error: No found account with ID " + userId);
		}
	}

	@PutMapping("/categories/{id}")
	@PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
	public ResponseEntity<?> updateCategories(HttpServletRequest request, @RequestBody CategoriesDTO newCate,
			@PathVariable(name = "id") int id) {
		String jwt = JwtAuthTokenFilter.parseJwt(request);
		String userId = jwtUtils.getUserNameFromJwtToken(jwt);
		try {
			person person = personService.getPerson(Integer.parseInt(userId)).get();
			boolean check = cateService.updateCategories(newCate);
			if (check)
				logger.info("Account id " + userId + " updated category id " + newCate.getId() + " success");
			else
				logger.error("Account id " + userId + " updated category id " + newCate.getId() + " failed");
			return check ? ResponseEntity.ok().body(new MessageResponse(StringUtils.TRUE)) 
					: ResponseEntity.badRequest().body(new MessageResponse(StringUtils.FALSE));
		} catch (IllegalArgumentException | ConstraintViolationException ex) {
			logger.error(ex.getMessage());
			return ResponseEntity.badRequest().body(new MessageResponse(StringUtils.FALSE));
		} catch (NoSuchElementException ex) {
			logger.error("Account id " + userId + " updated created id " + newCate.getId()
					+ " failed: No found account with ID " + userId);
			throw new ObjectNotFoundException("Error: No found account with ID " + userId);
		}
	}

	@Operation(summary = "Delete Categories")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "The request has succeeded", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = categories.class)) }),
			@ApiResponse(responseCode = "401", description = "Unauthorized, Need to login first!", content = @Content),
			@ApiResponse(responseCode = "400", description = "Bad Request: Invalid syntax", content = @Content),
			@ApiResponse(responseCode = "404", description = "Can not find the requested resource", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content) })
	@DeleteMapping("/categories/{id}")
	@PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
	public String deleteCategories(HttpServletRequest request, @PathVariable(name = "id") int id) {
		String jwt = JwtAuthTokenFilter.parseJwt(request);
		String userId = jwtUtils.getUserNameFromJwtToken(jwt);
		try {

			person person = personService.getPerson(Integer.parseInt(userId)).get();
			categories category = cateService.getCategories(id).get();
			boolean check = cateService.deleteCategories(category);
			if (check)
				logger.info("Account id " + userId + " delete category " + category.getId() + " success");
			else
				logger.error("Account id " + userId + " updated category" + category.getId() + " failed");
			return check ? StringUtils.TRUE : StringUtils.FALSE;
		} catch (DataIntegrityViolationException ex) {
			logger.error("Account id " + userId + " updated created id " + id
					+ " failed: This category had at least a product. Delete Product first!");
			throw new ObjectViolateForeignKeyException("This category had at least a product. Delete Product first!");
		} catch (NoSuchElementException ex) {
			logger.error("Account id " + userId + " updated created id " + id + " failed: No found account with ID "
					+ userId + " or category with id " + id);
			throw new ObjectNotFoundException(
					"Error: No found account with ID " + userId + " or category with id " + id);
		}
	}
}

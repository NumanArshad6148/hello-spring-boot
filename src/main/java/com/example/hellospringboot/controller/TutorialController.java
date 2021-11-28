package com.example.hellospringboot.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.hellospringboot.entity.Tutorial;
import com.example.hellospringboot.repository.TutorialRepository;

@RestController
@RequestMapping("/api")
public class TutorialController {

	@Autowired
	TutorialRepository tutorialRepository;

//	@GetMapping("/tutorials")
//	public ResponseEntity<List<Tutorial>> getAllTutorials() {
//		try {
//			return new ResponseEntity<List<Tutorial>>(tutorialRepository.findAll(), HttpStatus.OK);
//		} catch (Exception e) {
//			System.out.println(e.getMessage());
//			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//	}

	@GetMapping("/tutorials")
	// name = "title", defaultValue = "default value constant
	public ResponseEntity<List<Tutorial>> getAllTutorials(@RequestParam(required = false) String title) {
		try {

			List<Tutorial> tutorials = new ArrayList<Tutorial>();
			if (title == null) {
				tutorialRepository.findAll().forEach(tutorials::add);
			} else {
				tutorialRepository.findByTitleContaining(title).forEach(tutorials::add);
			}

//			if(tutorials.isEmpty()) {
//				return new ResponseEntity<>( HttpStatus.NO_CONTENT);
//			}
			return new ResponseEntity<List<Tutorial>>(tutorials, HttpStatus.OK);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/tutorials")
	public ResponseEntity<Tutorial> saveTutorial(@RequestBody Tutorial tutorial) {
		try {
			Tutorial _tutorial = tutorialRepository
					.save(new Tutorial(tutorial.getTitle(), tutorial.getDescription(), false));
			return new ResponseEntity<Tutorial>(_tutorial, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/tutorials/{id}")
	public ResponseEntity<?> updateTutorial(@PathVariable("id") long id, @RequestBody Tutorial tutorial) {
		Optional<Tutorial> tutorialData = tutorialRepository.findById(id);
		if (tutorialData.isPresent()) {
			Tutorial _tutorial = tutorialData.get();
			_tutorial.setTitle(tutorial.getTitle());
			_tutorial.setDescription(tutorial.getDescription());
			_tutorial.setPublished(tutorial.isPublished());

			return new ResponseEntity<Tutorial>(tutorialRepository.save(_tutorial), HttpStatus.OK);
		}
		return new ResponseEntity<>("Tutorial for id = " + id + "not found", HttpStatus.NOT_FOUND);
	}

	@GetMapping("/tutorials/{id}")
	public ResponseEntity<?> getTutorialById(@PathVariable("id") long id) {
		Optional<Tutorial> tutorialData = tutorialRepository.findById(id);
		if (tutorialData.isPresent()) {
			return new ResponseEntity<Tutorial>(tutorialData.get(), HttpStatus.OK);
		}
		return new ResponseEntity<>("Tutorial for id = " + id + "not found", HttpStatus.NOT_FOUND);

	}

	@DeleteMapping("/tutorials/{id}")
	public ResponseEntity<HttpStatus> deleteTutorialById(@PathVariable("id") long id) {
		try {
			tutorialRepository.deleteById(id);
			return new ResponseEntity<HttpStatus>(HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/tutorials/published")
	public ResponseEntity<?> getTutorialByPublished() {
		try {
			List<Tutorial> tutorials = tutorialRepository.findByPublished(true);
			if (tutorials.isEmpty()) {
				return new ResponseEntity<HttpStatus>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<List<Tutorial>>(tutorials, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/tutorials_pagination_sorting")
	public ResponseEntity<Map<String, Object>> getAllTutorialsPag(@RequestParam(defaultValue = "0") Integer pageNo,
			@RequestParam(defaultValue = "2") Integer pageSize, @RequestParam(defaultValue = "id") String sortBy,
			@RequestParam(defaultValue = "asc") String sortOrder, @RequestParam(required = false) String title) {
		try {
//		    Sort sort = Sort.by(sortBy);	
//		    return new ResponseEntity<List<Tutorial>>(tutorialRepository.findAll(sortOrder == "desc" ? sort.descending() : sort.ascending()), HttpStatus.OK);

			Sort sort = Sort.by(sortBy);
			Pageable paging = PageRequest.of(pageNo, pageSize,
					sortOrder == "desc" ? sort.descending() : sort.ascending());
			
			Page<Tutorial> pageResult = title == null ? tutorialRepository.findAll(paging)
					: tutorialRepository.findByTitleContaining(title, paging);
//			return new ResponseEntity<List<Tutorial>>(pageResult.getContent(), HttpStatus.OK);

			
			//customize response object
			Map<String, Object> response = new HashMap<String, Object>();
			
			response.put("tutorials", pageResult.getContent());
			response.put("count", pageResult.getTotalElements());
			response.put("totalPages", pageResult.getTotalPages());

			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}
}

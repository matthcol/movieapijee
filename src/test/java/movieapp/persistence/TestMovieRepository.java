package movieapp.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static testing.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.ActiveProfiles;

import movieapp.persistence.entity.Movie;
import movieapp.persistence.provider.MovieProvider;
import movieapp.persistence.repository.MovieRepository;
import testing.persistence.DatabaseUtils;

@DataJpaTest // active Spring Data avec sa couche JPA Hibernate
//@AutoConfigureTestDatabase(replace = Replace.NONE) // deactivate H2 +
//@ActiveProfiles("test") // + DB from application-test.properties
class TestMovieRepository {

	@Autowired
	private MovieRepository movieRepository;
	
	@Autowired
	private TestEntityManager entityManager;


	
	@Test
	void testFindByTitle() {
		// giving
		// 1 - a title of movies to read in the test  
		var title = "The Man Who Knew Too Much";
		var otherTitle = "The Man Who Knew Too Little";
		var goodTitles = List.of(title, title);
		var goodYears = List.of(1934, 1956);
		// 2 - writing data in database via the entity manager
		var moviesDatabase = MovieProvider.moviesGoodOnesOneBad(
				goodTitles, goodYears, otherTitle, 1997);
		DatabaseUtils.insertDataFlushAndClearCache(entityManager, moviesDatabase);
		// when : read from the repository
		var moviesFound = movieRepository.findByTitle(title)
				.collect(Collectors.toList());
		var movieTitlesFound = moviesFound.stream()
				.map(Movie::getTitle)
				.collect(Collectors.toList());
		var movieYearsFound = moviesFound.stream()
				.map(Movie::getYear)
				.collect(Collectors.toList());
		// then 
		assertSizeEquals(goodTitles, movieTitlesFound, "number of titles");
		assertAllEquals(title, movieTitlesFound, "title");
		assertCollectionUniqueElementEquals(goodYears, movieYearsFound, "year");
	}
	
	
	
	@ParameterizedTest
	@ValueSource(strings = {
			"Z", 
			"Blade Runner", 
			"Night of the Day of the Dawn of the Son of the Bride of the Return of the Revenge of the Terror of the Attack of the Evil Mutant Hellbound Flesh Eating Crawling Alien Zombified Subhumanoid Living Dead, Part 5"})
	void testSaveTitle(String title) {
		// given
		int year = 1982;
		int duration = 173;
		// when + then
		saveAssertMovie(title, year, duration);
	}
			
	@Test
	void testSaveTitleEmptyNOK() {
		String title = null;
		int year = 1982;
		int duration = 173;
		assertThrows(DataIntegrityViolationException.class, 
				() -> saveAssertMovie(title, year, duration));
	}
	
	@ParameterizedTest
	@ValueSource(ints = { 1888, 1982, Integer.MAX_VALUE })
	void testSaveYear(int year) {
		// given
		String title = "Blade Runner";
		int duration = 173;
		// when + then
		saveAssertMovie(title, year, duration);
	}
	
	@ParameterizedTest
	@ValueSource(ints = { 1, 120, Integer.MAX_VALUE })
	@NullSource
	void testSaveDuration(Integer duration) {
		// given
		String title = "Blade Runner";
		int year = 1982;
		// when + then
		saveAssertMovie(title, year, duration);
	}
	
	
	@Test
	void testSaveYearNullNOK() {
		// given
		String title = "Blade Runner";
		Integer year = null;
		int duration = 173;
		// when + then
		assertThrows(DataIntegrityViolationException.class,
				() -> saveAssertMovie(title, year, duration));
	}

	private void saveAssertMovie(String title, Integer year, Integer duration) {
		Movie movie = new Movie(title, year, duration);
		// when
		movieRepository.save(movie);
		// then
		var idMovie = movie.getId();
		assertNotNull(idMovie, "id generated by database");
		// NB : following test only checks that object read is the same as object written (cache)
		movieRepository.findById(idMovie)
			.ifPresent(m -> assertEquals(movie, m));
	}

}

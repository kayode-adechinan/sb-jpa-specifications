package com.adechinan.sbjpaspecifications;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.*;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

import static org.springframework.data.jpa.domain.Specification.where;


@Entity
@Data
@NoArgsConstructor
class Movie implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String title;
	private String genre;
	private double rating;
	private double watchTime;
	private int releaseYear;

	public Movie(String title, String genre, double rating, double watchTime, int releaseYear) {
		this.title = title;
		this.genre = genre;
		this.rating = rating;
		this.watchTime = watchTime;
		this.releaseYear = releaseYear;
	}

}

interface MovieRepository extends JpaRepository<Movie, Long>,
		JpaSpecificationExecutor<Movie> {

}

enum SearchOperation {
	GREATER_THAN,
	LESS_THAN,
	GREATER_THAN_EQUAL,
	LESS_THAN_EQUAL,
	NOT_EQUAL,
	EQUAL,
	MATCH,
	MATCH_START,
	MATCH_END,
	IN,
	NOT_IN
}


@Data
@NoArgsConstructor
class SearchCriteria {
	private String key;
	private Object value;
	private SearchOperation operation;

	public SearchCriteria(String key, Object value, SearchOperation operation) {
		this.key = key;
		this.value = value;
		this.operation = operation;
	}

}


class MovieSpecification implements Specification<Movie> {

	private List<SearchCriteria> list;

	public MovieSpecification() {
		this.list = new ArrayList<>();
	}

	public void add(SearchCriteria criteria) {
		list.add(criteria);
	}

	@Override
	public Predicate toPredicate(Root<Movie> root, CriteriaQuery<?> query, CriteriaBuilder builder) {

		//create a new predicate list
		List<Predicate> predicates = new ArrayList<>();

		//add add criteria to predicates
		for (SearchCriteria criteria : list) {
			if (criteria.getOperation().equals(SearchOperation.GREATER_THAN)) {
				predicates.add(builder.greaterThan(
						root.get(criteria.getKey()), criteria.getValue().toString()));
			} else if (criteria.getOperation().equals(SearchOperation.LESS_THAN)) {
				predicates.add(builder.lessThan(
						root.get(criteria.getKey()), criteria.getValue().toString()));
			} else if (criteria.getOperation().equals(SearchOperation.GREATER_THAN_EQUAL)) {
				predicates.add(builder.greaterThanOrEqualTo(
						root.get(criteria.getKey()), criteria.getValue().toString()));
			} else if (criteria.getOperation().equals(SearchOperation.LESS_THAN_EQUAL)) {
				predicates.add(builder.lessThanOrEqualTo(
						root.get(criteria.getKey()), criteria.getValue().toString()));
			} else if (criteria.getOperation().equals(SearchOperation.NOT_EQUAL)) {
				predicates.add(builder.notEqual(
						root.get(criteria.getKey()), criteria.getValue()));
			} else if (criteria.getOperation().equals(SearchOperation.EQUAL)) {
				predicates.add(builder.equal(
						root.get(criteria.getKey()), criteria.getValue()));
			} else if (criteria.getOperation().equals(SearchOperation.MATCH)) {
				predicates.add(builder.like(
						builder.lower(root.get(criteria.getKey())),
						"%" + criteria.getValue().toString().toLowerCase() + "%"));
			} else if (criteria.getOperation().equals(SearchOperation.MATCH_END)) {
				predicates.add(builder.like(
						builder.lower(root.get(criteria.getKey())),
						criteria.getValue().toString().toLowerCase() + "%"));
			} else if (criteria.getOperation().equals(SearchOperation.MATCH_START)) {
				predicates.add(builder.like(
						builder.lower(root.get(criteria.getKey())),
						"%" + criteria.getValue().toString().toLowerCase()));
			} else if (criteria.getOperation().equals(SearchOperation.IN)) {
				predicates.add(builder.in(root.get(criteria.getKey())).value(criteria.getValue()));
			} else if (criteria.getOperation().equals(SearchOperation.NOT_IN)) {
				predicates.add(builder.not(root.get(criteria.getKey())).in(criteria.getValue()));
			}
		}

		return builder.and(predicates.toArray(new Predicate[0]));
	}
}



@Data
@NoArgsConstructor
@Entity
class Customer{
	@Id
	@GeneratedValue
	private Long id;
	private String name;
	private int age;
	public Customer(String name, int age){
		this.name = name;
		this.age = age;
	}
}

@Repository
interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer>{}


class CustomerSpecifications {

	public static Specification<Customer> customerHasName(String name) {
			return (root, query, cb) ->  cb.equal(root.get("name"), name);
	}

	public static Specification<Customer> customerHasNotAge(int age) {
		return (root, query, cb) ->  cb.lessThan(root.get("age"), age);
	}

}




@Data
@NoArgsConstructor
@Entity
class Employee {
	private @Id
	@GeneratedValue
	Long id;
	private String name;
	private String dept;
	private int salary;

	public Employee(String name, String dept, int salary){
		this.name = name;
		this.dept = dept;
		this.salary = salary;
	}
}

@Repository
interface EmployeeRepository extends JpaRepository<Employee, Long>,
		QuerydslPredicateExecutor<Employee> {
}


class SpecificationChain {
	public static  <T> Specification<T> add(Specification<T> a, Specification<T> b) {
		return a.and(b);
	}
}


@SpringBootApplication
public class SbJpaSpecificationsApplication {

	public static void main(String[] args) {
		SpringApplication.run(SbJpaSpecificationsApplication.class, args);
	}

	@Bean
	public CommandLineRunner specificationsDemo(MovieRepository movieRepository,
												CustomerRepository customerRepository,
												EmployeeRepository employeeRepository) {
		return args -> {


			customerRepository.saveAll(Arrays.asList(
					new Customer("jacob", 12),
					new Customer("rober", 16)
			));


			employeeRepository.saveAll(Arrays.asList(
					new Employee("Diana", "Admin", 2000),
					new Employee("Mike", "Sales", 1000),
					new Employee("Rose", "IT", 4000),
					new Employee("Sara", "Admin", 3500),
					new Employee("Randy", "Sales", 3000),
					new Employee("Charlie", "IT", 2500)
			));



			/*BooleanExpression booleanExpression = QEmployee.employee.dept.in("IT", "Admin").and(
					QEmployee.employee.salary.between(3000, 5000));
			employeeRepository.findAll(booleanExpression)
					.forEach(System.out::println);*/






			/*customerRepository.findAll(CustomerSpecifications.customerHasName("jacob"))
					.forEach(System.out::println);
			customerRepository.findAll(CustomerSpecifications.customerHasNotAge(10))
					.forEach(System.out::println);*/



			/*Specification<Customer> specs = where(CustomerSpecifications.customerHasName("jacob"))
					.and(CustomerSpecifications.customerHasNotAge(16));

			customerRepository.findAll(specs).forEach(System.out::println);*/

			/*Specification<Customer> step1 = where(CustomerSpecifications.customerHasName("jacob"));
			step1.and(CustomerSpecifications.customerHasNotAge(16));

			customerRepository.findAll(step1).forEach(System.out::println);*/



			List<Specification<Customer>> specifications = List.of(
					CustomerSpecifications.customerHasName("jacob"),
					CustomerSpecifications.customerHasNotAge(16)
			);
			Specification<Customer> finalSpecs = specifications.stream().reduce(SpecificationChain::add).orElseThrow();
			customerRepository.findAll(finalSpecs).forEach(System.out::println);

			List<Specification<Customer>> specifications1 = List.of(
					CustomerSpecifications.customerHasName("jacob")
			);
			Specification<Customer> finalSpecs1 = specifications1.stream().reduce(SpecificationChain::add).orElseThrow();
			customerRepository.findAll(finalSpecs1).forEach(System.out::println);

			List<Specification<Customer>> specifications2 = new ArrayList<>();

			Map<?, ?> queryParams = Map.of("name", "jacob", "age", 16);

			queryParams.forEach((key, value) -> {
				switch (key.toString()) {
					case "name":
						specifications2.add(CustomerSpecifications.customerHasName(value.toString()));
						break;
					case "age":
						specifications2.add(CustomerSpecifications.customerHasNotAge(Integer.parseInt(value.toString())));
						break;
					/*default:
						throw new RuntimeException("Key doesn't exists" + key);*/
				}
			});

			System.out.println(specifications2.size());;






			/*
			// create new movies
			movieRepository.saveAll(Arrays.asList(
					new Movie("Troy", "Drama", 7.2, 196, 2004),
					new Movie("The Godfather", "Crime", 9.2, 178, 1972),
					new Movie("Invictus", "Sport", 7.3, 135, 2009),
					new Movie("Black Panther", "Action", 7.3, 135, 2018),
					new Movie("Joker", "Drama", 8.9, 122, 2018),
					new Movie("Iron Man", "Action", 8.9, 126, 2008)
			));


			// search movies by `genre`
			MovieSpecification msGenre = new MovieSpecification();
			msGenre.add(new SearchCriteria("genre", "Action", SearchOperation.EQUAL));
			List<Movie> msGenreList = movieRepository.findAll(msGenre);
			msGenreList.forEach(System.out::println);

			// search movies by `title` and `rating` > 7
			MovieSpecification msTitleRating = new MovieSpecification();
			msTitleRating.add(new SearchCriteria("title", "black", SearchOperation.MATCH));
			msTitleRating.add(new SearchCriteria("rating", 7, SearchOperation.GREATER_THAN));
			List<Movie> msTitleRatingList = movieRepository.findAll(msTitleRating);
			msTitleRatingList.forEach(System.out::println);

			// search movies by release year < 2010 and rating > 8
			MovieSpecification msYearRating = new MovieSpecification();
			msYearRating.add(new SearchCriteria("releaseYear", 2010, SearchOperation.LESS_THAN));
			msYearRating.add(new SearchCriteria("rating", 8, SearchOperation.GREATER_THAN));
			List<Movie> msYearRatingList = movieRepository.findAll(msYearRating);
			msYearRatingList.forEach(System.out::println);

			// search movies by watch time >= 150 and sort by `title`
			MovieSpecification msWatchTime = new MovieSpecification();
			msWatchTime.add(new SearchCriteria("watchTime", 150, SearchOperation.GREATER_THAN_EQUAL));
			List<Movie> msWatchTimeList = movieRepository.findAll(msWatchTime, Sort.by("title"));
			msWatchTimeList.forEach(System.out::println);

			// search movies by `title` <> 'white' and paginate results
			MovieSpecification msTitle = new MovieSpecification();
			msTitle.add(new SearchCriteria("title", "white", SearchOperation.NOT_EQUAL));

			Pageable pageable = PageRequest.of(0, 3, Sort.by("releaseYear").descending());
			Page<Movie> msTitleList = movieRepository.findAll(msTitle, pageable);

			msTitleList.forEach(System.out::println);


			// combine using `AND` operator
			List<Movie> movies = movieRepository.findAll(Specification.where(msTitle).and(msYearRating));
			movies.forEach(System.out::println);



			// combine using `OR` operator
			List<Movie> movies1 = movieRepository.findAll(Specification.where(msTitle).or(msYearRating));
			movies1.forEach(System.out::println);


			*/
		};
	}
}

package com.ogm.hrms.repository;

import com.ogm.hrms.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByEmployeeCodeIgnoreCase(String employeeCode);

    boolean existsByEmployeeCodeIgnoreCaseAndIdNot(String employeeCode, Long id);

    boolean existsByUser_Id(Long userId);

    boolean existsByUser_IdAndIdNot(Long userId, Long id);

    @EntityGraph(attributePaths = {"department", "designation", "employmentType"})
    Page<Employee> findByDeletedFalse(Pageable pageable);

    @EntityGraph(attributePaths = {"department", "designation", "employmentType", "user"})
    Optional<Employee> findByIdAndDeletedFalse(Long id);

    Optional<Employee> findByUser_IdAndDeletedFalse(Long userId);

    long countByDeletedFalse();

    long countByDeletedFalseAndJoiningDateGreaterThanEqual(java.time.LocalDate from);

    @org.springframework.data.jpa.repository.Query(
            "select coalesce(d.name, 'Unassigned'), count(e) from Employee e left join e.department d "
                    + "where e.deleted = false group by d.name order by count(e) desc")
    java.util.List<Object[]> departmentDistribution();

    @org.springframework.data.jpa.repository.Query(
            "select e.gender, count(e) from Employee e where e.deleted = false group by e.gender")
    java.util.List<Object[]> genderDistribution();

    @org.springframework.data.jpa.repository.Query(
            "select e from Employee e where e.deleted = false "
                    + "and (e.dateOfBirth is not null or e.joiningDate is not null)")
    java.util.List<Employee> findActiveWithKeyDates();

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"department", "designation"})
    java.util.List<Employee> findByDeletedFalseOrderByEmployeeCodeAsc();

    /** Active employees that have a birthday or joining date, with their user account eagerly loaded. */
    @org.springframework.data.jpa.repository.Query(
            "select e from Employee e left join fetch e.user where e.deleted = false "
                    + "and (e.dateOfBirth is not null or e.joiningDate is not null)")
    java.util.List<Employee> findActiveWithUserAndKeyDates();

    /** All active employees with their user account eagerly loaded (for broadcast-style automations). */
    @org.springframework.data.jpa.repository.Query(
            "select e from Employee e left join fetch e.user where e.deleted = false")
    java.util.List<Employee> findAllActiveWithUser();

    /** Active employees whose employment/contract end date falls within [from, to], with user loaded. */
    @org.springframework.data.jpa.repository.Query(
            "select e from Employee e left join fetch e.user where e.deleted = false "
                    + "and e.endDate between :from and :to")
    java.util.List<Employee> findActiveWithEndDateBetween(java.time.LocalDate from, java.time.LocalDate to);

    /** Free-text search over code, full name, and official email (for the AI assistant / lookups). */
    @EntityGraph(attributePaths = {"department", "designation", "employmentType"})
    @org.springframework.data.jpa.repository.Query(
            "select e from Employee e where e.deleted = false and ("
                    + "lower(e.employeeCode) like lower(concat('%', :q, '%')) or "
                    + "lower(e.fullName) like lower(concat('%', :q, '%')) or "
                    + "lower(e.contact.officialEmail) like lower(concat('%', :q, '%')))")
    Page<Employee> search(String q, Pageable pageable);
}

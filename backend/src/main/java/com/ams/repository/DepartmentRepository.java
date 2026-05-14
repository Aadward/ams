package com.ams.repository;

import com.ams.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findByParentIsNull();

    List<Department> findByParentId(Long parentId);

    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.children WHERE d.parent IS NULL")
    List<Department> findAllRootWithChildren();
}

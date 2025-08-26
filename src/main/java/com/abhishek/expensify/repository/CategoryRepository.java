package com.abhishek.expensify.repository;

import com.abhishek.expensify.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity,Long> {

    List<CategoryEntity> findByProfileId(Long profile);

    Optional<CategoryEntity> findByIdAndProfileId(Long id, Long profile);

    List<CategoryEntity> findByTypeAndProfileId(String type,Long profile);

     Boolean existsByNameAndProfileId(String name,Long profile);

}

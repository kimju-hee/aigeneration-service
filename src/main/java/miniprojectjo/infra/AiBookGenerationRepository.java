package miniprojectjo.infra;

import miniprojectjo.domain.AiBookGeneration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource(
    collectionResourceRel = "aiBookGenerations",
    path = "aiBookGenerations"
)
public interface AiBookGenerationRepository extends JpaRepository<AiBookGeneration, Long> {
    Optional<AiBookGeneration> findByManuscriptId(Long manuscriptId);
}

package www.founded.com.controller.payment.client_freelancer_contract;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import www.founded.com.dto.client_freelancer_contract.JobPostCreateDTO;
import www.founded.com.enum_.client_freelancer_contract.JobPostStatus;
import www.founded.com.mapper.JobsPostMapper;
import www.founded.com.model.client.Client;
import www.founded.com.model.payment.client_freelancer_contract.JobPost;
import www.founded.com.repository.client_freelancer_contract.JobPostRepository;


@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobPostController {

    private final JobPostRepository jobPostRepo;
    private final JobsPostMapper jpm;

    // Client creates job post
    @PostMapping
    public ResponseEntity<JobPost> create(@Valid @RequestBody JobPostCreateDTO dto) {
        JobPost job = new JobPost();
        job.setClientId(jpm.toJobPost(dto));
        job.setTitle(dto.getTitle());
        job.setDescription(dto.getDescription());
        job.setBudget(dto.getBudget());
        job.setCurrency(dto.getCurrency() == null ? "USD" : dto.getCurrency());
        job.setStatus(JobPostStatus.OPEN);
        return ResponseEntity.ok(jobPostRepo.save(job));
    }

    // Public listing (freelancer sees)
    @GetMapping
    public ResponseEntity<List<JobPost>> listOpen() {
        return ResponseEntity.ok(jobPostRepo.findByStatus(JobPostStatus.OPEN));
    }

    // Client views their own jobs
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<JobPost>> listByClient(@PathVariable Client clientId) {
        return ResponseEntity.ok(jobPostRepo.findByClientId(clientId));
    }

    // Close job post
    @PostMapping("/{jobId}/close")
    public ResponseEntity<Void> close(@PathVariable Long jobId) {
        JobPost job = jobPostRepo.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));
        job.setStatus(JobPostStatus.CLOSED);
        jobPostRepo.save(job);
        return ResponseEntity.ok().build();
    }
    // Search jobs by query (for global search)
    @GetMapping("/search")
    public ResponseEntity<List<JobPost>> searchJobs(@RequestParam("query") String query) {
        List<JobPost> results = jobPostRepo.findAll().stream()
            .filter(job -> (job.getTitle() != null && job.getTitle().toLowerCase().contains(query.toLowerCase())) ||
                          (job.getId() != null && job.getId().toString().contains(query)))
            .toList();
        return ResponseEntity.ok(results);
    }
}
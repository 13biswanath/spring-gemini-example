package com.example.demo;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;


@RestController
@RequestMapping("/api")
public class WorkoutController {

    private final BackendService backendService;
    private final WorkoutPlanService workoutPlanService;

    public WorkoutController(BackendService backendService,
                             WorkoutPlanService workoutPlanService) {
        this.backendService = backendService;
        this.workoutPlanService = workoutPlanService;
    }

    @PostMapping("/generate-plan")
    public ResponseEntity<String> generatePlan(@RequestBody PlanRequest request,
                                               HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("NOT_LOGGED_IN");
        }

        String prompt = buildPrompt(request);
        String responseHtml = backendService.getAiResponse(prompt);
        return ResponseEntity.ok(responseHtml);
    }

    @PostMapping("/plans")
    public ResponseEntity<Void> savePlan(@RequestBody SavePlanRequest request,
                                         HttpSession session) {

        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        workoutPlanService.savePlan(
                username,
                request.getTitle(),
                request.getGoal(),
                request.getDaysPerWeek(),
                request.getEquipment(),
                request.getDiet(),
                request.getPlanText()
        );

        return ResponseEntity.ok().build();
    }

    @GetMapping("/plans")
    public ResponseEntity<List<WorkoutPlanService.WorkoutPlanDto>> getPlans(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<WorkoutPlanService.WorkoutPlanDto> plans =
                workoutPlanService.getPlansForUser(username);

        return ResponseEntity.ok(plans);
    }

    private String buildPrompt(PlanRequest r) {
        StringBuilder sb = new StringBuilder();
        sb.append("Create a detailed 7-day workout and meal plan.\n");
        sb.append("Fitness goal: ").append(r.getGoal()).append("\n");
        sb.append("Days per week available: ").append(r.getDaysPerWeek()).append("\n");
        sb.append("Available equipment: ").append(r.getEquipment()).append("\n");
        sb.append("Diet preference: ").append(r.getDiet()).append("\n");
        sb.append("Additional notes: ").append(r.getNotes()).append("\n");
        sb.append("Format as HTML using headings and lists, NO <html> or <body> tags.");
        return sb.toString();
    }

    // Request DTOs

    public static class PlanRequest {
        private String goal;
        private String daysPerWeek;
        private String equipment;
        private String diet;
        private String notes;

        public String getGoal() {
            return goal;
        }
        public void setGoal(String goal) {
            this.goal = goal;
        }
        public String getDaysPerWeek() {
            return daysPerWeek;
        }
        public void setDaysPerWeek(String daysPerWeek) {
            this.daysPerWeek = daysPerWeek;
        }
        public String getEquipment() {
            return equipment;
        }
        public void setEquipment(String equipment) {
            this.equipment = equipment;
        }
        public String getDiet() {
            return diet;
        }
        public void setDiet(String diet) {
            this.diet = diet;
        }
        public String getNotes() {
            return notes;
        }
        public void setNotes(String notes) {
            this.notes = notes;
        }
    }

    public static class SavePlanRequest {
        private String title;
        private String goal;
        private String daysPerWeek;
        private String equipment;
        private String diet;
        private String planText;

        public String getTitle() {
            return title;
        }
        public void setTitle(String title) {
            this.title = title;
        }
        public String getGoal() {
            return goal;
        }
        public void setGoal(String goal) {
            this.goal = goal;
        }
        public String getDaysPerWeek() {
            return daysPerWeek;
        }
        public void setDaysPerWeek(String daysPerWeek) {
            this.daysPerWeek = daysPerWeek;
        }
        public String getEquipment() {
            return equipment;
        }
        public void setEquipment(String equipment) {
            this.equipment = equipment;
        }
        public String getDiet() {
            return diet;
        }
        public void setDiet(String diet) {
            this.diet = diet;
        }
        public String getPlanText() {
            return planText;
        }
        public void setPlanText(String planText) {
            this.planText = planText;
        }
    }
}
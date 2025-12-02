package com.example.demo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;

@Service
public class WorkoutPlanService {

    private final Datastore datastore;
    private final KeyFactory keyFactory;

    // In-memory fallback if Datastore is unavailable (for local only)
    private final List<WorkoutPlanDto> inMemoryPlans =
            Collections.synchronizedList(new ArrayList<>());
    private long inMemoryIdCounter = 1L;

    public WorkoutPlanService() {
        Datastore tmpDs = null;
        KeyFactory tmpKf = null;
        try {
            tmpDs = DatastoreOptions.getDefaultInstance().getService();
            tmpKf = tmpDs.newKeyFactory().setKind("WorkoutPlan");
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.datastore = tmpDs;
        this.keyFactory = tmpKf;
    }

    public void savePlan(String username,
                         String title,
                         String goal,
                         String daysPerWeek,
                         String equipment,
                         String diet,
                         String planText) {

        // If Datastore is available, use it
        if (datastore != null && keyFactory != null) {
            try {
                Key key = datastore.allocateId(keyFactory.newKey());
                Entity entity = Entity.newBuilder(key)
                        .set("username", username)
                        .set("title", orEmpty(title))
                        .set("goal", orEmpty(goal))
                        .set("daysPerWeek", orEmpty(daysPerWeek))
                        .set("equipment", orEmpty(equipment))
                        .set("diet", orEmpty(diet))
                        .set("planText", orEmpty(planText))
                        .set("createdAt", Timestamp.now())
                        .build();
                datastore.put(entity);
                return; // done, no need for fallback
            } catch (DatastoreException e) {
                e.printStackTrace();
            }
        }

        // Fallback: in-memory per user
        WorkoutPlanDto dto = new WorkoutPlanDto();
        synchronized (this) {
            dto.setId(inMemoryIdCounter++);
        }
        dto.setUsername(username);
        dto.setTitle(orEmpty(title));
        dto.setGoal(orEmpty(goal));
        dto.setDaysPerWeek(orEmpty(daysPerWeek));
        dto.setEquipment(orEmpty(equipment));
        dto.setDiet(orEmpty(diet));
        dto.setPlanText(orEmpty(planText));
        dto.setCreatedAt(Timestamp.now().toString());

        inMemoryPlans.add(dto);
    }

    public List<WorkoutPlanDto> getPlansForUser(String username) {
        List<WorkoutPlanDto> result = new ArrayList<>();

        // If Datastore is available, only use Datastore
        if (datastore != null && keyFactory != null) {
            try {
                Query<Entity> query = Query.newEntityQueryBuilder()
                        .setKind("WorkoutPlan")
                        .setFilter(StructuredQuery.PropertyFilter.eq("username", username))
                        .setOrderBy(StructuredQuery.OrderBy.desc("createdAt"))
                        .build();

                QueryResults<Entity> results = datastore.run(query);
                while (results.hasNext()) {
                    Entity e = results.next();
                    WorkoutPlanDto dto = new WorkoutPlanDto();
                    dto.setId(e.getKey().getId());
                    dto.setUsername(e.getString("username"));
                    dto.setTitle(e.getString("title"));
                    dto.setGoal(e.getString("goal"));
                    dto.setDaysPerWeek(e.getString("daysPerWeek"));
                    dto.setEquipment(e.getString("equipment"));
                    dto.setDiet(e.getString("diet"));
                    dto.setPlanText(e.getString("planText"));
                    dto.setCreatedAt(e.getTimestamp("createdAt").toString());
                    result.add(dto);
                }
                return result;
            } catch (DatastoreException e) {
                e.printStackTrace();
            }
        }

        // Fallback: use in-memory list, but only this user’s plans
        synchronized (inMemoryPlans) {
            for (WorkoutPlanDto dto : inMemoryPlans) {
                if (username.equals(dto.getUsername())) {
                    result.add(dto);
                }
            }
        }

        return result;
    }

    private String orEmpty(String s) {
        return (s == null) ? "" : s;
    }

    // DTO class
    public static class WorkoutPlanDto {
        private long id;
        private String username;
        private String title;
        private String goal;
        private String daysPerWeek;
        private String equipment;
        private String diet;
        private String planText;
        private String createdAt;

        public long getId() { return id; }
        public void setId(long id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getGoal() { return goal; }
        public void setGoal(String goal) { this.goal = goal; }

        public String getDaysPerWeek() { return daysPerWeek; }
        public void setDaysPerWeek(String daysPerWeek) { this.daysPerWeek = daysPerWeek; }

        public String getEquipment() { return equipment; }
        public void setEquipment(String equipment) { this.equipment = equipment; }

        public String getDiet() { return diet; }
        public void setDiet(String diet) { this.diet = diet; }

        public String getPlanText() { return planText; }
        public void setPlanText(String planText) { this.planText = planText; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }
}

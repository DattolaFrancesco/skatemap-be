package fra.skatemap.config;

import fra.skatemap.entities.Spot;
import fra.skatemap.entities.SpotType;
import fra.skatemap.entities.Type;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class SpotSpecification {
    public static Specification<Spot> hasContinent(List<String> continent){
        return(root, query, criteriaBuilder) -> {
            if(continent == null) return null;
            return root.get("continents").in(continent);
        };
    }
    public static Specification<Spot> hasRisk(List<String> risk){
        return(root, query, criteriaBuilder) -> {
            if(risk == null) return null;
            return root.get("risk").in(risk);
        };
    }
    public static Specification<Spot> hasType(List<String> type){
        return(root, query, criteriaBuilder) -> {
            if (type == null || type.isEmpty()) return null;
            List<Predicate> predicates = new ArrayList<>();
            for (String t : type) {
                Join<Spot, SpotType> spotTypeJoin = root.join("spotTypes", JoinType.INNER);
                Join<SpotType, Type> typeJoin = spotTypeJoin.join("type", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(typeJoin.get("spotType"), t));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    public static Specification<Spot> hasSearch(String search){
        return(root, query, criteriaBuilder) -> {
            if (search == null || search.isEmpty()) return null;
            Predicate cityPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("city")),"%"+search.toLowerCase()+"%");
            Predicate streetPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("street")),"%"+search.toLowerCase()+"%");
            Predicate namePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),"%"+search.toLowerCase()+"%");
            Predicate continentPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("continents")),"%"+search.toLowerCase()+"%");
            return criteriaBuilder.or(cityPredicate,streetPredicate,namePredicate,continentPredicate);
        };
    }
}

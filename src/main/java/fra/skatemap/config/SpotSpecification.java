package fra.skatemap.config;

import fra.skatemap.chatBot.DTO.SpotSearchParamsDTO;
import fra.skatemap.entities.Spot;
import fra.skatemap.entities.SpotType;
import fra.skatemap.entities.Type;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    public static Specification<Spot> hasStatus(String status){
        return(root, query, criteriaBuilder) -> {
            if(status == null) return null;
            return criteriaBuilder.equal(root.get("status"), status);
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
    public static Specification<Spot> hasCity(String city) {
        return (root, query, criteriaBuilder) -> {
            if (city == null || city.isEmpty()) return null;
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("city")),
                    "%" + city.toLowerCase()
            );
        };
    }

    public static Specification<Spot> hasCountry(String country) {
        return (root, query, criteriaBuilder) -> {
            if (country == null || country.isEmpty()) return null;
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("country")),
                    "%" + country.toLowerCase() + "%"
            );
        };
    }
    public static Specification<Spot> hasSearch(String search){
        return(root, query, criteriaBuilder) -> {
            if (search == null || search.isEmpty()) return null;
            Predicate cityPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("city")),"%"+search.toLowerCase()+"%");
            Predicate streetPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("street")),"%"+search.toLowerCase()+"%");
            Predicate namePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),"%"+search.toLowerCase()+"%");
            Predicate countryPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("country")),"%"+search.toLowerCase()+"%");
            Predicate continentPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("continents")),"%"+search.toLowerCase()+"%");
            return criteriaBuilder.or(cityPredicate,streetPredicate,namePredicate,countryPredicate,continentPredicate);
        };
    }
    public static Specification<Spot> build(SpotSearchParamsDTO params) {
        return Specification
                .where(hasContinent(params.continents() != null ? List.of(params.continents()) : null))
                .and(hasRisk(params.risk() != null ? List.of(params.risk()) : null))
                .and(hasType(params.type() != null ? params.type().stream().map(String::toUpperCase).collect(Collectors.toList()) : null))
                .and(hasCity(params.city() != null ? params.city().toUpperCase() : null))
                .and(hasCountry(params.country() != null ? params.country().toUpperCase() : null));
    }
}

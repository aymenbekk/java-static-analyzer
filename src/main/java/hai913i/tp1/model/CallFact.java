package hai913i.tp1.model;

import java.util.Optional;

// targetId est vide pour un appel non resolu : il est compte, mais ne produit pas d'arc.
public record CallFact(String callerId, Optional<String> targetId, String staticReceiverType,
         int sourceLine, boolean resolved, boolean internal) {

}

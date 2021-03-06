package org.march.sync.transform;

import org.march.data.model.Command;
import org.march.data.command.Nil;
import org.march.data.command.Set;
import org.march.data.command.Unset;

public class UnsetSetInclusion implements Inclusion {
  
    public boolean canInclude(Command o1, Command o2) {
        return o1 instanceof Unset && o2 instanceof Set;
    }

    public Command include(Command o1, Command o2, boolean inferior) {
        Unset   u1 = (Unset)o1;
        Set     s1 = (Set)o2;
        
        if (u1.getIdentifier().equals(s1.getIdentifier())){
            return Nil.instance();
        }
        
        return o1;
    }
}

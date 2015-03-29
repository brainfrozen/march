package org.march.sync;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import org.march.data.CommandException;
import org.march.data.Model;
import org.march.data.ObjectException;
import org.march.data.Operation;
import org.march.data.simple.SimpleModel;
import org.march.sync.endpoint.EndpointException;
import org.march.sync.endpoint.LeaderEndpoint;
import org.march.sync.endpoint.Message;
import org.march.sync.endpoint.MessageHandler;
import org.march.sync.endpoint.OutboundEndpoint;
import org.march.sync.transform.Transformer;

public class Leader {
    
    private HashMap<UUID, LeaderEndpoint> channels;    
   
    private Transformer transformer;
    
    private Clock clock;
    
    private Model model;
    
    private ReentrantLock lock;
    
    public Leader(Transformer transformer){
        this.channels    = new HashMap<UUID, LeaderEndpoint>();               
        this.clock       = new Clock();
        
        this.transformer    = transformer;
        this.model          = new SimpleModel();
        
        this.lock   = new ReentrantLock();
    }
    
    public void subscribe(UUID member){
        if(!this.channels.containsKey(member)){
            final LeaderEndpoint channel = new LeaderEndpoint(this.transformer, this.lock);
            
            this.channels.put(member, channel);        
            
            channel.onInbound(new MessageHandler() {                
                public void handle(Message message) {
                    Leader.this.inbound(channel, message);
                }
            }); 
        }        
    }
    
    public void unsubscribe(UUID member){        
        LeaderEndpoint channel = this.channels.remove(member);
        if(channel != null){
            channel.offInbound();
        }        
    }
    
    public OutboundEndpoint getOutbound(UUID member){
        return this.channels.get(member);     
    }
    
    private void inbound(LeaderEndpoint originChannel, Message message){        
        this.clock.tick();
        
        try {
            // TODO: set recovery point on model
            for(Operation operation: message.getOperations()){
                this.model.apply(operation.getPointer(), operation.getCommand());
            }
        } catch (ObjectException|CommandException  e) {
            // TODO: roll already performed changes back to recovery point
            // TODO: send error to memnber
            
            unsubscribe(message.getMember());   
            
            return;
        } 
        
        for(LeaderEndpoint channel: this.channels.values()){
            if(channel != originChannel){
                
                //TODO: filter Nil type commands - no need to forward
                // need a deep copy of operations since operations are mutable
                Operation[] operations = new Operation[message.getOperations().length];
                for(int i = 0; i < operations.length; i++){
                    operations[i] = message.getOperations()[i].clone();
                }
                              
                try {
                    channel.send(new Message(message.getMember(), channel.getRemoteTime(), this.clock.getTime(), operations));
                } catch (EndpointException e) {
                    // kill channel
                }
            }
        }
    }
        
}

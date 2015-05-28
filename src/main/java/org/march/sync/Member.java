package org.march.sync;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import org.march.data.Command;
import org.march.data.CommandException;
import org.march.data.Data;
import org.march.data.Model;
import org.march.data.ObjectException;
import org.march.data.Operation;
import org.march.data.Pointer;
import org.march.data.simple.SimpleModel;
import org.march.sync.endpoint.EndpointException;
import org.march.sync.endpoint.MemberEndpoint;
import org.march.sync.endpoint.Message;
import org.march.sync.endpoint.UpdateMessage;
import org.march.sync.endpoint.MessageHandler;
import org.march.sync.endpoint.OutboundEndpoint;
import org.march.sync.transform.Transformer;


public class Member {
    // add state
    private UUID name;  
    
    private Clock clock;
    
    private MemberEndpoint channel;
    
    private Model model;
    
    private HashSet<CommandHandler> commandHandlers = new HashSet<CommandHandler>();
    
    public Member(UUID name, Transformer transformer){
        this.name = name; 
        
        clock   = new Clock();
        channel = new MemberEndpoint(transformer);
        
        model   = new SimpleModel();
                
        channel.onInbound(new MessageHandler() {            
            public void handle(Message message) {
                try {
                    for(Operation operation: message.getOperations()){
                        Member.this.model.apply(operation.getPointer(), operation.getCommand());
                        
                        for(CommandHandler handler: Member.this.commandHandlers){
                            handler.handleCommand(operation.getPointer(), operation.getCommand());
                        }
                    }
                } catch (ObjectException|CommandException  e) {
                   channel.offInbound();
                } 
            }
        });
    }

    public OutboundEndpoint getOutbound(){
        return this.channel;
    }
        
    public void apply(Pointer pointer, Command command) throws MemberException{        
        try {
            model.apply(pointer, command);
            
            UpdateMessage message = new UpdateMessage(this.name, clock.tick(), channel.getRemoteTime(), 
                    new Operation[]{new Operation(pointer, command)});
            
            channel.send(message);
        } catch (EndpointException e) {
            //TODO: check reinit on channelexception
            throw new MemberException(e);
        } catch (ObjectException|CommandException e){
            throw new MemberException(e);
        }      
    }
    
    public Data find(Pointer pointer, String identifier)
            throws ObjectException, CommandException {
        return model.find(pointer, identifier);
    }

    public Data find(Pointer pointer, int index) throws ObjectException,
            CommandException {
        return model.find(pointer, index);
    }

    public void onCommand(CommandHandler... handlers){
        this.commandHandlers.addAll(Arrays.asList(handlers));        
    }
    
    public void offCommand(CommandHandler... handlers){
        if(handlers.length == 0){
            this.commandHandlers.clear();
        }
    }
    
}
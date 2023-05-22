%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% 
%                               NEW PREDICATES                                %
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

token(Q):- type(_,Q). %(pou einai, typos) 

place(P):- ptarc(P,_,_,_). %(apo place, to transition, token, quantity)                  
place(P):- tparc(_,P,_,_). %(apo transition, to place, token, quantity)                                   
place(P):- ptarcb(P,_,_,_,_,_).             
place(P):- tparcb(_,P,_,_,_,_).                  

trans(T):- ptarc(_,T,_,_).        
trans(T):- tparc(T,_,_,_).   
trans(T):- ptarcb(_,T,_,_,_,_).     
trans(T):- tparcb(T,_,_,_,_,_).      

holdsbonds(P,Q2,Q1,TS):-holdsbonds(P,Q1,Q2,TS).

same(Q,Q) :- token(Q).

has(P,Q,TS):-holds(P,Q,TS).
has(P,Q,TS):-holdsbonds(P,Q,_,TS).


% █▀▀ █▀█ █▀█ █░█░█ ▄▀█ █▀█ █▀▄   █▀▀ ▀▄▀ █▀▀ █▀▀ █░█ ▀█▀ █ █▀█ █▄░█
% █▀░ █▄█ █▀▄ ▀▄▀▄▀ █▀█ █▀▄ █▄▀   ██▄ █░█ ██▄ █▄▄ █▄█ ░█░ █ █▄█ █░▀█

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%                               ENABLING ASSIGNMENT                           %
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

{satisfied(P,T,V,Q,TS)} :-  ptarc(P,T,V,X), has(P,Q,TS),
                            not unavailable(Q,TS),
                            type(X,Q), time(TS).

{satisfied(P,T,V1,Q1,V2,Q2,TS)} :-  ptarcb(P,T,V1,T1,V2,T2),        % for a ptarcb
                                    holdsbonds(P,Q1,Q2,TS),         % if it has the bond needed
                                    type(T1,Q1), type(T2,Q2),       % of the correct type,
                                    not same(Q1,Q2),                % tokens are not the same
                                    not unavailable(Q1,TS),         % and both are not unavailable
                                    not unavailable(Q2,TS),         % then might be satisfied 
                                    not splitcycle(P,Q1,Q2,TS),     % if its not undecidable
                                    time(TS).              

% Because bonds might break or be formed, assign single tokens to single functions and not bonds.
assigned(P,T,V1,Q1,TS) :-   satisfied(P,T,V1,Q1,TS), time(TS).          % assign token Q1 to V1
assigned(P,T,V1,Q1,TS;P,T,V2,Q2,TS) :-   satisfied(P,T,V1,Q1,V2,Q2,TS), time(TS). 

% if more than 1 Variable is assigned to a Token Q atm, then discard the set
numVarAsgnToToken(P,T,Q,C,TS):-   C=#count{A:assigned(P,T,A,Q,TS)}, assigned(P,T,_,Q,TS).                       
:-numVarAsgnToToken(P,T,Q,C,TS), C>1 , time(TS).

% if more than 1 Token is assigned to a Variable V atm, then discard the set
numTokensAsgnToVar(P,T,V,C,TS):-   C=#count{A:assigned(P,T,V,A,TS)}, assigned(P,T,V,_,TS).
:-numTokensAsgnToVar(P,T,V,C,TS), C>1 , time(TS).


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%                             SATISFIED CONDITIONS                            %
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% If Q1 and Q2 are chained after breaking Q1 and Q2, then they are in a circle and its undecidable how
% to split them on the seperating bond.
splitcycle(P,Q1,Q2,TS) :-   splitbond(P,T,V1,T1,V2,T2), chain(P,Q1,Q2,Q1,Q2,TS), type(T1,Q1), type(T2,Q2).

chain(P,Q1,Q2,A,B,TS) :-    holdsbonds(P,Q1,Q2,TS),            % IDEA:
                            holdsbonds(P,A,B,TS),              % create the chain between all bonds 
                            not same(Q1,A), not same(Q1,B),    % except the seperating bond A and B.
                            splitbond(P,T,V1,T1,V2,T2),        
                            type(T1,A), type(T2,B),                             
                            time(TS). 

chain(P,Q1,Q2,A,B,TS) :-    chain(P,Q1,Q3,A,B,TS),      % if Q1-Q3-Q2 is chained                                 
                            chain(P,Q2,Q3,A,B,TS),      % then Q1 is chained with Q2
                            not same(Q1,Q2),
                            time(TS).
 
chain(P,Q1,Q2,A,B,TS) :-    chain(P,Q2,Q1,A,B,TS).


unavailable(Q,TS) :-    holdsbonds(P,Q,QQ,TS),          % a not assigned Token
                        not assigned(P,_,_,Q,TS),       % is unavailable
                        assigned(P,_,_,QQ,TS),          % if it is bonded with an assigned token
                        time(TS).

unavailable(Q,TS) :-    holdsbonds(P,Q,QQ,TS),          % a not assigned Token
                        not assigned(P,_,_,Q,TS),       % is unavailable
                        unavailable(QQ,TS),             % if it is bonded with an unavailable token
                        time(TS).


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%                                    ENABLED                                   %
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% T is enabled if the number of distinct ptarcb/6 is equal to the number of satisfied/7 arcs
enabled(T,TS) :-    C1=#count{P,V1,T1,V2,T2:ptarcb(P,T,V1,T1,V2,T2), place(P)}, 
                    C1{satisfied(_,T,_,_,_,_,TS)}C1, 
                    C2=#count{P,V1,T1:ptarc(P,T,V1,T1), place(P)},
                    C2{satisfied(_,T,_,_,TS)}C2, 
                    C1 + C2 > 0,
                    trans(T), time(TS).


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%					                  FIRE  	                        	   %
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

{fires(T,TS)} :- enabled(T,TS), time(TS).

firing(C,TS) :- C1=#count{T:fires(T,TS)}, C2=#count{T1:firesb(T1,TS)}, C = C1 + C2, time(TS).
:-firing(C,TS), C>1, time(TS).                      
:-firing(C,TS), C=0, enabled(T,TS), time(TS).      
:-firing(C,TS), C=0, enabled_coll(T,TS),time(TS).


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%					               AFTER FIRE  	                               %
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

splitbond(P,T,V1,T1,V2,T2) :-   ptarcb(P,T,V1,T1,V2,T2),   
                                not tparcb(T,_,V1,T1,V2,T2). 

splittokens(T,Q1,Q2 ; T,Q2,Q1) :-   splitbond(P,T,V1,T1,V2,T2),
                                    assigned(PT,T,V1,Q1,TS), assigned(PT,T,V2,Q2,TS),
                                    time(TS).


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%     NOT ASSIGNED TOKENS / BONDS      %     	                        	  
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

holds(P,Q,TS+1) :-  fires(T,TS),
                    holds(P,Q,TS),
                    not assigned(P,T,_,Q,TS),
                    not unavailable(Q,TS),         
                    time(TS).

holdsbonds(P,Q1,Q2,TS+1) :- fires(T,TS),
                            holdsbonds(P,Q1,Q2,TS),   
                            not assigned(P,T,_,Q1,TS),
                            not unavailable(Q1,TS),
                            time(TS).


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%       ASSIGNED TOKENS / BONDS        %    	                        	  
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% single token transfer
% it could also execute when a bond is broken after execution of T
holds(P,Q1,TS+1):-  fires(T,TS),                           % P now holds Q1         
                    tparc(T,P,V1,T1),                      % if V1:T1 comes to P,                
                    assigned(PT,T,V1,Q1,TS), type(T1,Q1),  % Q1 was assigned to V1
                    not holdsbonds(PT,Q1,_,TS),            % and Q1 was not boneded
                    time(TS).

% OR Q1 was boned but the bond broke and left Q1 unbonded
holds(P,Q1,TS+1):-  fires(T,TS),
                    tparc(T,P,V1,T1),                      % if V1:T1 comes to P                
                    assigned(PT,T,V1,Q1,TS), type(T1,Q1),  % Q1 was assigned to V1.
                    holdsbonds(PT,Q1,Q2,TS),               % Q1 was bonded with Q2
                    not holdsbonds(PT,Q1,Q3,TS),           % only
                    token(Q3), not same(Q2,Q3), 
                    splittokens(T,Q1,Q2),                    % but the bond broke
                    time(TS).

% a new bond was Formed from T, connecting tokens from PT1 and PT2
holdsbonds(P,Q1,Q2,TS+1):-  fires(T,TS),
                            tparcb(T,P,V1,T1,V2,T2),
                            assigned(PT1,T,V1,Q1,TS), type(T1,Q1),
                            assigned(PT2,T,V2,Q2,TS), type(T2,Q2),
                            time(TS).


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   BONDED TO ASSIGNED TOKENS / BONDS  %    	                        	  
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Situation: tparc(P,T,V1,Q1) but Q1 was bonded 
holdsbonds(P,Q1,Q2,TS+1):-  fires(T,TS),                            
                            holdsbonds(PT,Q1,Q2,TS),                % if a token Q2 was bonded at PT,
                            assigned(PT,T,V1,Q1,TS), type(T1,Q1),   % with an assigned token V1:Q1, 
                            not splittokens(T,Q1,Q2),                 % the bond didnt break
                            tparc(T,P,V1,T1),                       % and Q1 was coming to P
                            unavailable(Q2,TS),                     % then move the whole bond to P
                            time(TS).                                  

% Situation: a bond transfered to P was bonded with other tokens as well before, so we also move them.
holdsbonds(P,Q1,Q2,TS+1):-  fires(T,TS),   
                            holdsbonds(P,Q1,_,TS+1),      % if P holds Q1            
                            holdsbonds(PT,Q1,Q2,TS),      % it should hold its bonds also
                            not splittokens(T,Q1,Q2),       % if they didnt break
                            unavailable(Q2,TS),                  
                            time(TS).


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%                   ASSIGN ALL TOKENS THAT CAN BE ASSIGNED  	               %
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%   
                                                                % REMOVE WORLDS WHERE:
:-  has(P,Q,TS), type(T1,Q), ptarc(P,T,V1,T1),                    % a place P holds an available                             
    not satisfied(P,T,V1,_,TS),                                     % and appropriate token,
    not assigned(P,T,_,Q,TS), not unavailable(Q,TS), time(TS).      % but does not get assigned to V1
       
:-  holdsbonds(P,Q1,Q2,TS), ptarcb(P,T,V1,T1,V2,T2),                % a place P holds an available
    type(T1,Q1), type(T2,Q2),                                       % and appropriate bond
    not satisfied(P,T,V1,_,V2,_,TS),                                % but does not get assigned
    not assigned(P,T,_,Q1,TS), not unavailable(Q1,TS),
    not assigned(P,T,_,Q2,TS), not unavailable(Q2,TS),
    not splitcycle(P,Q1,Q2,TS), time(TS).



% █▀█ █▀▀ █░█ █▀▀ █▀█ █▀ █ █▄░█ █▀▀   █▀▀ ▀▄▀ █▀▀ █▀▀ █░█ ▀█▀ █ █▀█ █▄░█
% █▀▄ ██▄ ▀▄▀ ██▄ █▀▄ ▄█ █ █░▀█ █▄█   ██▄ █░█ ██▄ █▄▄ █▄█ ░█░ █ █▄█ █░▀█


execcounter(T, 0, 0)  :-  trans(T).
execcounter(T,C,TS+1) :-  not fires(T,TS), not firesb(T,TS), execcounter(T,C,TS), time(TS+1).
execcounter(T,C+1,TS+1) :-  fires(T,TS), execcounter(T,C,TS), time(TS+1).
execcounter(T,C-1,TS+1) :-  firesb(T,TS), execcounter(T,C,TS), time(TS+1).                            


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%                               ENABLING ASSIGNMENT                           %
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

{coll_satisfied(P,T,V,Q,TS)} :- execcounter(T,C,TS), C>0,
                                tparc(T,P,V,X),
                                has(P,Q,TS), type(X,Q),
                                not coll_unavailable(Q,TS),
                                time(TS).

{coll_satisfied(P,T,V1,Q1,V2,Q2,TS)} :- execcounter(T,C,TS), C>0,
                                        tparcb(T,P,V1,T1,V2,T2),            % for a ptarcb
                                        holdsbonds(P,Q1,Q2,TS),             % if it has the bond needed
                                        type(T1,Q1), type(T2,Q2),           % of the correct type,
                                        not same(Q1,Q2),                    % tokens are not the same
                                        not coll_unavailable(Q1,TS),        % and both are not unavailable
                                        not coll_unavailable(Q2,TS),        % then might be satisfied 
                                        not coll_splitcycle(P,Q1,Q2,TS),    % if its not undecidable
                                        time(TS).        

coll_assigned(P,T,V1,Q1,TS) :-   coll_satisfied(P,T,V1,Q1,TS), time(TS).          % assign token Q1 to V1
coll_assigned(P,T,V1,Q1,TS;P,T,V2,Q2,TS) :-   coll_satisfied(P,T,V1,Q1,V2,Q2,TS), time(TS). 

% if more than 1 Variable is coll-assigned to a Token Q atm, then discard the set
numVarCollAsgnToToken(P,T,Q,C,TS):-   C=#count{A:coll_assigned(P,T,A,Q,TS)}, coll_assigned(P,T,_,Q,TS).                       
:-numVarCollAsgnToToken(P,T,Q,C,TS), C>1 , time(TS).

% if more than 1 Token is coll-assigned to a Variable V atm, then discard the set
numTokensCollAsgnToVar(P,T,V,C,TS):-   C=#count{A:coll_assigned(P,T,V,A,TS)}, coll_assigned(P,T,V,_,TS).  
:-numTokensCollAsgnToVar(P,T,V,C,TS), C>1 , time(TS).


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%                          COLL SATISFIED CONDITIONS                          %
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

coll_splitcycle(P,Q1,Q2,TS):- coll_splitbond(P,T,V1,T1,V2,T2), chain(P,Q1,Q2,Q1,Q2,TS), type(T1,Q1), type(T2,Q2).

chain(P,Q1,Q2,A,B,TS) :-    holdsbonds(P,Q1,Q2,TS),            % IDEA:
                            holdsbonds(P,A,B,TS),              % create the chain between all bonds 
                            not same(Q1,A), not same(Q1,B),    % except the seperating bond A and B.
                            execcounter(T,C,TS), C>0,
                            coll_splitbond(P,T,V1,T1,V2,T2),        
                            type(T1,A), type(T2,B),                             
                            time(TS). 

coll_unavailable(Q,TS) :-   holdsbonds(P,Q,QQ,TS),              % a not assigned Token
                            not coll_assigned(P,_,_,Q,TS),      % is unavailable
                            coll_assigned(P,_,_,QQ,TS),         % if it is bonded with an assigned token
                            time(TS).

coll_unavailable(Q,TS) :-   holdsbonds(P,Q,QQ,TS),          % a not assigned Token
                            not coll_assigned(P,_,_,Q,TS),  % is unavailable
                            coll_unavailable(QQ,TS),        % if it is bonded with an unavailable token
                            time(TS).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%                                 COLL-ENABLED                                 %
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% T is enabled if the number of distinct ptarcb/6 is equal to the number of satisfied/7 arcs
enabled_coll(T,TS) :-   C1=#count{P,V1,T1,V2,T2:tparcb(T,P,V1,T1,V2,T2), place(P)}, 
                        C1{coll_satisfied(_,T,_,_,_,_,TS)}C1, 
                        C2=#count{P,V1,T1:tparc(T,P,V1,T1), place(P)},
                        C2{coll_satisfied(_,T,_,_,TS)}C2, 
                        C1 + C2 > 0, 
                        trans(T), time(TS).


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%					                FIRE BACK	                        	   %
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

{firesb(T,TS)} :- enabled_coll(T,TS), time(TS).


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%					          AFTER REVERSE FIRE  	                           %
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

coll_splitbond(P,T,V1,T1,V2,T2) :-  tparcb(T,P,V1,T1,V2,T2),  
                                    not ptarcb(_,T,V1,T1,V2,T2).      
                                    
coll_splittokens(T,Q1,Q2 ; T,Q2,Q1) :-  coll_splitbond(P,T,V1,T1,V2,T2),
                                        coll_assigned(TP,T,V1,Q1,TS), coll_assigned(TP,T,V2,Q2,TS),
                                        time(TS).


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%     NOT ASSIGNED TOKENS / BONDS      %     	                        	  
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

holds(P,Q,TS+1):-   firesb(T,TS),
                    holds(P,Q,TS),
                    not coll_assigned(P,T,_,Q,TS),
                    not coll_unavailable(Q,TS),         
                    time(TS).

holdsbonds(P,Q1,Q2,TS+1):-  firesb(T,TS),
                            holdsbonds(P,Q1,Q2,TS),   
                            not coll_assigned(P,T,_,Q1,TS),
                            not coll_unavailable(Q1,TS),
                            time(TS).


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%       ASSIGNED TOKENS / BONDS        %    	                        	  
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% single token transfer
% it could also execute when a bond is broken after execution of T
holds(P,Q1,TS+1):-  firesb(T,TS),                               % P now holds Q1         
                    ptarc(P,T,V1,T1),                           % if V1:T1 comes to P,                
                    coll_assigned(TP,T,V1,Q1,TS), type(T1,Q1),  % Q1 was assigned to V1
                    not holdsbonds(TP,Q1,_,TS),                 % and Q1 was not boneded
                    time(TS).

% OR Q1 was boned but the bond broke and left Q1 unbonded
holds(P,Q1,TS+1):-  firesb(T,TS),
                    ptarc(P,T,V1,T1),                           % if V1:T1 comes to P                
                    coll_assigned(TP,T,V1,Q1,TS), type(T1,Q1),  % Q1 was assigned to V1.
                    holdsbonds(TP,Q1,Q2,TS),                    % Q1 was bonded with Q2
                    not holdsbonds(TP,Q1,Q3,TS),                % only
                    token(Q3), not same(Q2,Q3), 
                    coll_splittokens(T,Q1,Q2),                    % but the bond broke
                    time(TS).

% a new bond was Formed from T, connecting tokens from PT1 and PT2
% OR if TP1==TP2 then a bond was transfered
holdsbonds(P,Q1,Q2,TS+1):-  firesb(T,TS),
                            ptarcb(P,T,V1,T1,V2,T2),
                            coll_assigned(TP1,T,V1,Q1,TS), type(T1,Q1),
                            coll_assigned(TP2,T,V2,Q2,TS), type(T2,Q2),
                            time(TS).


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   BONDED TO ASSIGNED TOKENS / BONDS  %    	                        	  
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Situation: tparc(P,T,V1,Q1) but Q1 was bonded 
holdsbonds(P,Q1,Q2,TS+1):-  firesb(T,TS),                            
                            holdsbonds(TP,Q1,Q2,TS),                     % if a token Q2 was bonded at TP,
                            coll_assigned(TP,T,V1,Q1,TS), type(T1,Q1),   % with an assigned token V1:Q1, 
                            not coll_splittokens(T,Q1,Q2),                 % the bond didnt break
                            ptarc(P,T,V1,T1),                            % and Q1 was coming to P
                            coll_unavailable(Q2,TS),
                            time(TS).                                    % then move the whole bond to P

% Situation: a bond transfered to P was bonded with other tokens as well before, so we also move them.
holdsbonds(P,Q1,Q2,TS+1):-  firesb(T,TS),   
                            holdsbonds(P,Q1,_,TS+1),            % if P holds Q1            
                            holdsbonds(TP,Q1,Q2,TS),            % it should hold its bonds also
                            not coll_splittokens(T,Q1,Q2),        % if they didnt break
                            coll_unavailable(Q2,TS),
                            time(TS).


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%                 COLL-ASSIGN ALL TOKENS THAT CAN BE ASSIGNED  	               %
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%   
                                                               
:-  has(P,Q,TS), type(T1,Q), tparc(T,P,V1,T1),                                                
    not coll_satisfied(P,T,V1,_,TS),                                  
    not coll_assigned(P,T,_,Q,TS), 
    not coll_unavailable(Q,TS), 
    execcounter(T,C,TS), C>0,
    time(TS).                                                    
    
:-  holdsbonds(P,Q1,Q2,TS), tparcb(T,P,V1,T1,V2,T2),                
    type(T1,Q1), type(T2,Q2), time(TS),                            
    not coll_satisfied(P,T,V1,_,V2,_,TS),                             
    not coll_assigned(P,T,_,Q1,TS), not coll_unavailable(Q1,TS),
    not coll_assigned(P,T,_,Q2,TS), not coll_unavailable(Q2,TS),
    execcounter(T,C,TS), C>0,
    not coll_splitcycle(P,Q1,Q2,TS).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%					                  SHOW  	                           	   %
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
#show time/1.
#show fires/2.
#show firesb/2.
#show holds/3.
#show holdsbonds/4.
#show enabled/2.
#show time/1.
#show assigned/5.
#show holds/3.
#show satisfied/7.
#show satisfied/5.
#show enabled/2.
#show holdsbonds/4.
#show fires/2.
#show unavailable/2.
#show splitcycle/4.
#show splitbond/3.
#show enabled_coll/2.
% #show chain/6.
#show coll_unavailable/2.
% #show unavailable/2.
% #show satisfied/7.
% #show satisfied/5.
% #show assigned/5.
#show coll_assigned/5.
% #show splitcycle/4.
% #show splitbond/3.
% #show splitbond/6.
% #show coll_splitbond/6.
% #show execcounter/3.
#show holdsbonds/4.

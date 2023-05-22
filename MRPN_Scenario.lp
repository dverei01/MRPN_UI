
holds(p1,b1,0).
holds(p2,b2,0).

type(a,b1).
type(b,b2).

ptarc(p1,t1,a1,a).
ptarc(p2,t1,b1,b).

tparc(t1,p3,b1,b).
tparc(t1,p4,a1,a).
time(0..4).

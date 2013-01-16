#include"hashset.h"
#include<string.h>
#include<stdio.h>

size_t h_capacities[] = {127,251,521, 1019, 2027, 4079, 8123, 16267, 32503, 65011, 130027,
                         260111, 520279, 1040387, 2080763, 4161539, 8323151, 16646323
                        };
int h_add_helper(Node** head, Node* nep, size_t capaicty, Hashset* set);

void h_initialize(Hashset* set) {
    set->size = 0;
    set->capacityStep = 0;
    set->head = malloc(sizeof(Node*) * h_capacities[set->capacityStep]);
    memset(set->head, 0, h_capacities[set->capacityStep]*sizeof(Node*));
}
int h_add(Hashset* set, Node* nep) {
    if((double)set->size/(double)h_capacities[set->capacityStep]>0.7) {
        h_rehash(set);
    }
    if(h_add_helper(set->head, nep, h_capacities[set->capacityStep], set)) {

        set->size++;
        return 1;
    }
    return 0;
}
int h_add_helper(Node** head, Node* nep, size_t capacity, Hashset* set) {
    int homeSlot = nd_hashCode(nep) % capacity;
    int slotNumberAfterProbing = homeSlot;
    int i;
    for(i=0;; i++) {
        if (!head[slotNumberAfterProbing]) {
            break;
        }

        if(nd_equals(head[slotNumberAfterProbing], nep)) {
            return 0;
        }
        slotNumberAfterProbing = (homeSlot + (i*i+i)/2) % capacity;

    }

    head[slotNumberAfterProbing] = nep;
    return 1;

}
int h_addAll(Hashset* set, Hashset* that) {
    int i;
    int count=0;
    for(i=0; i<h_capacities[that->capacityStep]; i++) {
        if(that->head[i]) {
            if(h_add(set, that->head[i]))
                count++;
        }
    }
    return count;
}
Node* h_find(Hashset* set, Node* ep) {
    int homeSlot = nd_hashCode(ep)%(h_capacities[set->capacityStep]);
    int slotNumberAfterProbing = homeSlot;
    int i;
    for(i=0;; i++) {
        if (!set->head[slotNumberAfterProbing]) {
            return 0;
        }
        if(nd_equals(set->head[slotNumberAfterProbing], ep)) {
            return set->head[slotNumberAfterProbing];
        }
        slotNumberAfterProbing = (homeSlot + (i*i+i)/2) % h_capacities[set->capacityStep] ;
    }
}

void h_rehash(Hashset* set) {
    size_t newCapacity = h_capacities[set->capacityStep + 1];
    size_t oldCapacity = h_capacities[set->capacityStep];
    Node** newHead = malloc(sizeof(Node*) * newCapacity);
    memset(newHead, 0,sizeof(Node*) * newCapacity );
    int i;
    for (i=0; i< oldCapacity; i++) {
        if (set->head[i]) {
            h_add_helper(newHead, set->head[i], newCapacity, set);
        }
    }
    free(set->head);
    set->head = newHead;
    set->capacityStep++;

}
void h_cleanUp(Hashset* set) {
    free(set->head);
}
void h_display(Hashset* set) {
    int i;
    for(i=0; i<h_capacities[set->capacityStep]; i++) {
        if (set->head[i]) {
            printf("%8d: %s\n",i,set->head[i]->word);
        }
    }

}
void h_deepCleanUp(Hashset* set) {
    int i;
    for(i=0; i<h_capacities[set->capacityStep]; i++) {
        if(set->head[i]) {
            nd_cleanUp(set->head[i]);
        }
    }
    h_cleanUp(set);
}

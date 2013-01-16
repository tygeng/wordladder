#include<string.h>
#include<stdio.h>
#include"vector.h"

void v_reallocate(Vector* vec);
void v_initialize(Vector* vec, size_t capacity) {
    vec->capacity = capacity;
    vec->head = malloc(sizeof(Node*)*capacity);
    vec->size = 0;
}
void v_add(Vector* vec, Node* newElement) {
    if(vec->size == vec->capacity) {
        v_reallocate(vec);
    }

    (vec->head)[vec->size++] = newElement;
}
void v_reallocate(Vector* vec) {
    Node** newHead = malloc(sizeof(Node*)*vec->capacity*2);
    memcpy(newHead, vec->head, vec->size*sizeof(Node*));
    free(vec->head);
    vec->head = newHead;
    vec->capacity *=2;
}
void v_cleanUp(Vector* vec) {
    free(vec->head);
    vec->head = 0;
}
void v_deepCleanUp(Vector* vec) {
    int i;
    for(i=0; i<vec->size; i++) {
        nd_cleanUp(vec->head[i]);
    }
    v_cleanUp(vec);
}
void v_display(Vector* vec) {
	if(vec->size==0) return;
    int i;
	printf("%s",vec->head[0]->word);
    for(i=1; i<vec->size; i++) {
        printf(", %s",vec->head[i]->word);
    }
	printf("\n");
}

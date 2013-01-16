#ifndef VECTOR
#define VECTOR
#include<stdlib.h>
typedef struct Node Node;
struct Vector {
    Node** head;
    size_t capacity;
    size_t size;
};

typedef struct Vector Vector;
#include"node.h"
void v_initialize(Vector* vec, size_t capacity);
void v_add(Vector* vec, Node* newElement);
void v_finish(Vector* vec);
void v_cleanUp(Vector* vec);
void v_deepCleanUp(Vector* vec);
void v_display(Vector* vec);
#endif

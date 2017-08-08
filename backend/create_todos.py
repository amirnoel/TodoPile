from firebase import firebase


firebase = firebase.FirebaseApplication('https://todo-pile.firebaseio.com', authentication=None)


def create_todos():
    file = open('todos.txt','r')
    for line in file:
        title = line.strip()
        todo = {'title':title,'description':title}
        response = firebase.post('/todos', todo, params={'print': 'silent'}, headers={'X_FANCY_HEADER': 'VERY FANCY'})
        print response

create_todos()

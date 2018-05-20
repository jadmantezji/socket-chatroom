# socket-chatroom
A simple socket chatroom client and server written as an excercise. Implemented in Java, built with Gradle.

## Issues to be sorted out
The code hasn't been touched for quite a long time, as I focused on Scala. It needs several improvements :).

### Concurrency issues
- `CopyOnWrite` and `ConcurrentHashMap` for listeners
- separate send and read locks
- synchronized `isPossibleToWrite`, as `Socket` class is not thread-safe
- perhaps synchronize on connection instead of `Communicator` object (to be evaluated)
- don't start threads in constructor
- simplify concurrency? (by using JMM principles - `happens-before`, etc.)

### Design
- immutable data type (Message) should have `final` fields
- consider better interface segregation
- fewer parameters in interfaces method signatures
- consider using new Java features (streams, etc.)
- consider decoupling client and server by using some IDL to describe interface between them

### Other
- widnow dispose
- use swing event thread
- thread pool size limit
- `System.exit` usage is kind of ugly - think of a better way?


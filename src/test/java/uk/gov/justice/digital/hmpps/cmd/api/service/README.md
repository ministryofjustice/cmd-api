I have noticed in kotlin, some test files with heavy use of localDate/time take a long time for the IDE to parse and analysise.
Sometimes the file will refuse to open at all in the IDE. If a file refuses to open, you can right click and mark it as plain text.
Then you will have to reduce the amount of lines like this:

`CsrDetailDto(DetailParentType.OVERTIME, day1.atTime(LocalTime.of(20,15)), day2.atTime(LocalTime.of(12,30)), "Nights OSG")`

Where test data is duplicated across tests I have been moving them to global variables. 
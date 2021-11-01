import java.lang.Appendable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// Pueden usar este archivo para hacer pruebas rápidas,
// de la misma forma en que usaban el REPL de Wollok.

// OJO: lo que esté aquí no será tenido en cuenta
// en la corrección ni reemplaza a los tests.

listOf("734", "8", "10", "10", "70", "900").groupingBy { it }.eachCount().maxByOrNull { it.value }!!.key

val lista1 = listOf("hola como están", "adios", "hola","hola como estas", "hola que tal", "hola,buenas tardes")

lista1.count{ it.contains("hola")}


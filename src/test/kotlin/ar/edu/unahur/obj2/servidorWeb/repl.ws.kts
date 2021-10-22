// Pueden usar este archivo para hacer pruebas rápidas,
// de la misma forma en que usaban el REPL de Wollok.

// OJO: lo que esté aquí no será tenido en cuenta
// en la corrección ni reemplaza a los tests.

listOf(1, 8, 10).average()

val url = "http://hola.com.ar/ruta/ruta2/ruta3/api.html"

val url2 = url.split("/").subList(3,url.split("/").size)
url2


url2[0] + "/" + url2[1] + "/" + url2[2] + "/" + url2[3]
url2.size

url2.joinToString("/")


/*var i = 0

var msj = ""

while (i < url2.size){
    msj = msj + "/" + url2[i]
    i += 1
}*/

//msj

fun protocolo() = url.substringBefore(":")

protocolo()


fun extension() = url.substringAfterLast(".")

extension()

url.substringAfter(".ar")

fun probar(num: Int) : Int{
    var numerito = 0
    if (num > 5){
        numerito = 89
    }
    return numerito
}

probar(7)








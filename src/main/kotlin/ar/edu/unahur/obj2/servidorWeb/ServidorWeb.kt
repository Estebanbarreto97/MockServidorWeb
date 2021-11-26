package ar.edu.unahur.obj2.servidorWeb

import ar.edu.unahur.obj2.servidorWeb.analizadores.Analizador
import ar.edu.unahur.obj2.servidorWeb.analizadores.AnalizadorIPsSospechosas
import ar.edu.unahur.obj2.servidorWeb.integraciones.Consola

object ServidorWeb {
  val modulos = mutableListOf<Modulo>()
  val analizadores = mutableListOf<Analizador>()

  fun agregarAnalizador(analizador : Analizador){
    analizadores.add(analizador)}

  fun removerAnalizador(analizador: Analizador){
    analizadores.remove(analizador)
  }

  fun obtenerProtocoloUrl(url: String) = url.substringBefore(":")

  fun obtenerExtensionUrl(url: String) = url.substringAfterLast(".")

  fun obtenerRutaUrl(url: String) = url.split("/").subList(3,url.split("/").size).joinToString("/")

  fun moduloElegido(pedido: PedidoHttp) = modulos.find{x -> x.extension.any{x -> x == obtenerExtensionUrl(pedido.url)}}

  fun respuestaDelModulo(pedido: PedidoHttp): RespuestaHttp {
    lateinit  var respuesta : RespuestaHttp
    if (obtenerProtocoloUrl(pedido.url) != "http") {
      respuesta = RespuestaHttp(CodigoHttp.NOT_IMPLEMENTED,"", 10, pedido)
    }
    else {
      if (modulos.any { x -> x.extension.any { x -> x == obtenerExtensionUrl(pedido.url) } }) {
        respuesta = RespuestaHttp(CodigoHttp.OK, moduloElegido(pedido)!!.devuelve, moduloElegido(pedido)!!.tiempo, pedido)
      } else {
        respuesta = RespuestaHttp(CodigoHttp.NOT_FOUND, "", 10, pedido)
      }
    }
    if(respuesta.codigo != CodigoHttp.NOT_FOUND)
      enviarAnalizar(respuesta, moduloElegido(pedido)!!)
    else
      enviarAnalizar(respuesta, Modulo(emptyList(),"",10,""))
    return respuesta
  }

  fun agregarModulo(unModulo: Modulo) { modulos.add(unModulo) }

  fun removerModulo(unModulo: Modulo) { modulos.remove(unModulo) }



  fun enviarAnalizar(respuesta : RespuestaHttp, modulo: Modulo){
    analizadores.forEach{it.analizar(respuesta, modulo)}
  }
}

class Modulo(val extension: List<String>, val devuelve: String, val tiempo: Int, val nombre: String)
package ar.edu.unahur.obj2.servidorWeb

import ar.edu.unahur.obj2.servidorWeb.integraciones.Consola

object ServidorWeb {
  val modulos = mutableListOf<Modulo>()

  fun procesar(pedido: PedidoHttp): RespuestaHttp {
    if (obtenerProtocoloUrl(pedido.url) != "http") {
      return RespuestaHttp(CodigoHttp.NOT_IMPLEMENTED,"", 10, pedido)
    }
    else {
      return RespuestaHttp(CodigoHttp.OK, "cualquier cosa", 200, pedido)
    }
  }

  fun obtenerProtocoloUrl(url: String) = url.substringBefore(":")

  fun obtenerExtensionUrl(url: String) = url.substringAfterLast(".")

  fun obtenerRutaUrl(url: String): String {
    val ruta = url.split("/").subList(3,url.split("/").size)

    return ruta.joinToString("/")
  }

  fun respuestaDelModulo(pedido: PedidoHttp): RespuestaHttp {
    val moduloElegido = modulos.find{x -> x.extension.any{x -> x == obtenerExtensionUrl(pedido.url)}}
    if (modulos.any{x -> x.extension.any{x -> x == obtenerExtensionUrl(pedido.url)}}) {
      return RespuestaHttp(CodigoHttp.OK, moduloElegido!!.devuelve, moduloElegido!!.tiempo, pedido)
    }
    else {
      return RespuestaHttp(CodigoHttp.NOT_FOUND, "", 10, pedido)
    }
  }

  fun agregarModulo(unModulo: Modulo) { modulos.add(unModulo) }

  fun removerModulo(unModulo: Modulo) { modulos.remove(unModulo) }

  class Modulo(val extension: List<String>, val devuelve: String, val tiempo: Int)

}

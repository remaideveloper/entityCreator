package utils

import scala.jdk.CollectionConverters.SeqHasAsJava
import scala.language.experimental.macros
import scala.reflect.runtime.universe._
import scala.language.experimental.macros
import scala.language.implicitConversions

object EntityUtils {
  def createEntity[T](implicit ct: TypeTag[T]): T = {
    val expr = Map(
      typeOf[String].toString -> "Str",
      typeOf[Int].toString -> 1,
      typeOf[Long].toString -> 5L,
      typeOf[Double].toString -> 3.25D,
    )
    createEntity(ct, expr).asInstanceOf[T]
  }

  private def createEntity[T](tt: TypeTag[T], expr: Map[String, Any]): Any = {
    val ct = tt.tpe
    val companion = ct.typeConstructor
    expr.get(companion.toString)
      .map(_.asInstanceOf[T])
      .getOrElse {
        companion match {
          case _ if companion <:< typeOf[Seq[_]].typeConstructor =>
            List(
              createEntity(
                typeToTypeTag(ct.typeArgs.head, tt.mirror),
                expr
              )
            ).asInstanceOf[T]

          case _ if companion <:< typeOf[java.util.Collection[_]].typeConstructor =>
            List(
              createEntity(
                typeToTypeTag(ct.typeArgs.head, tt.mirror),
                expr
              )
            ).asJava.asInstanceOf[T]

          case _ if companion =:= typeOf[Option[_]].typeConstructor =>
            Option(
              createEntity(
                typeToTypeTag(ct.typeArgs.head, tt.mirror),
                expr
              )
            ).asInstanceOf[T]

          case _ =>
            val constructors = companion.decls.filter(f => f.isMethod && f.name == termNames.CONSTRUCTOR)
            // Пытаемся найти конструктор без параметров, если нет то берем первый попавшийся
            val constructor = constructors.find(_.asMethod.paramLists.head.isEmpty).map(_.asMethod).getOrElse(constructors.head.asMethod)
            val constructorParamTypes = constructor.paramLists.head.map(_.typeSignature)
            val constructorEntity = constructorParamTypes.map { paramType =>
              /*
                  Решение бага, если передать в конструктор циклическую зависимость, то зацикливается
                  Всё равно дальше через рефлексию переопределяем поля
               */
              val nExpr = expr + (companion.toString -> null)
              createEntity(typeToTypeTag(paramType, tt.mirror), nExpr)
            }
            val obj = tt.mirror.reflectClass(companion.typeSymbol.asClass).reflectConstructor(
              constructor
            ).apply(constructorEntity: _*)
            val nExpr = expr + (companion.toString -> obj) // разрешение циклической зависимости на уровне инициализации параметров
            val objMirror = tt.mirror.reflect(obj)
            val fields = ct.decls.filter(f => f.isTerm && !f.isMethod).map(_.asTerm)
            fields.foreach { field =>
              val fieldSymbol = ct.decl(TermName(field.name.toString)).asTerm
              objMirror.reflectField(fieldSymbol).set(createEntity(typeToTypeTag(field.typeSignature, tt.mirror), nExpr))
            }
            obj.asInstanceOf[T]
        }
      }
  }

  private def typeToTypeTag[T](
                                tpe: Type,
                                mirror: reflect.api.Mirror[reflect.runtime.universe.type]
                              ): TypeTag[T] = {
    TypeTag(mirror, new reflect.api.TypeCreator {
      def apply[U <: reflect.api.Universe with Singleton](m: reflect.api.Mirror[U]): U#Type = {
        assert(m eq mirror, s"TypeTag[$tpe] defined in $mirror cannot be migrated to $m.")
        tpe.asInstanceOf[U#Type]
      }
    })
  }
}

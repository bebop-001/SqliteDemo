import Cities.name
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object Cities: IntIdTable() {
    val name = varchar("name", 50)
}

class City(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<City>(Cities)

    var name by Cities.name
}
fun main(args: Array<String>) {
    // creates /home/sjs/dev/Exposed/KotlinExposed/exposed.sqlite.mv.db
    // val dbFile = File("./exposed.sqlite")
    val dbFile = File("${System.getenv("PWD")}/exposed.sqlite")
    println("PWD:${System.getenv("PWD")}, dbFile:$dbFile")

    // h2 should be sqlite but doesn't crash.
    // Generates: SQL: INSERT INTO CITIES ("NAME") VALUES ('St. Petersburg')
    // Creates: $PWD/exposed.sqlite.mv.db
    // Database.connect("jdbc:h2:$dbFile", driver = "org.sqlite.JDBC")

    // crashes with SQLite supports only TRANSACTION_SERIALIZABLE and TRANSACTION_READ_UNCOMMITTED.
    // creates 0 length $PWD/exposed.sqlite
    Database.connect("jdbc:sqlite:$dbFile", driver = "org.sqlite.JDBC")

    transaction {
        // print sql to std-out
        addLogger(StdOutSqlLogger)

        SchemaUtils.create(Cities)
    }
    transaction {
        // insert new city. SQL: INSERT INTO Cities (name) VALUES ('St. Petersburg')
        City.new {
            name = "St. Petersburg"
        }

        // 'select *' SQL: SELECT Cities.id, Cities.name FROM Cities
        println("Cities: ${City.all()}")
    }
}

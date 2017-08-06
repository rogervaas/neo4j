/*
 * Copyright (c) 2002-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.internal.cypher.acceptance

import org.neo4j.cypher.{ExecutionEngineFunSuite, NewPlannerTestSupport}

class MultipleGraphsAcceptanceTest extends ExecutionEngineFunSuite with NewPlannerTestSupport {

  test("from graph") {
    val query = "FROM GRAPH test AT 'graph://url' MATCH (a)-->() RETURN a"
    expect(query) doesNotSupport "FROM"
  }

  test("into graph") {
    val query = "MATCH (a)--() INTO GRAPH test AT 'graph://url' CREATE (a)-->(b:B) RETURN b"
    expect(query) doesNotSupport "INTO"
  }

  test("return named graph") {
    val query = "WITH $param AS foo MATCH ()--() RETURN 1 GRAPHS foo"

    expect(query) doesNotSupport "returning graphs"
  }

  test("return all graphs") {
    val query = "MATCH ()--() RETURN 1 GRAPHS *"

    expect(query) doesNotSupport "returning graphs"
  }

  private final case class expect(query: String) {
    import scala.util.{Failure, Success, Try}

    def doesNotSupport(clause: String): Unit = {
      Try(executeWithCostPlannerAndInterpretedRuntimeOnly(query)) match {
        case _: Success[_] => fail(s"Expected $clause to be unsupported")
        case Failure(exception) =>
          exception.getMessage should include(s"$clause is not supported")
      }
    }
  }
}

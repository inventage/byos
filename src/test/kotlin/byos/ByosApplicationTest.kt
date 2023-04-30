package byos

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ByosApplicationTest {

    @Test
    fun `simple query`() {
        val query = """
            query {
              allBooks {
                id
                title
                publishedin
              }
            }
        """

        val ast = parseASTFromQuery(query)
        val trees = buildInternalQueryTree(ast)
        val result = trees.map { tree ->
            executeJooqQuery { ctx ->
                ctx.select(resolveInternalQueryTree(tree)).fetch()
            }
        }.formatGraphQLResponse()

        val expectedResult = """
            {
              "data": {
                "allBooks": [
                  {
                    "id": 1,
                    "title": "1984",
                    "publishedin": 1948
                  },
                  {
                    "id": 2,
                    "title": "Animal Farm",
                    "publishedin": 1945
                  },
                  {
                    "id": 3,
                    "title": "O Alquimista",
                    "publishedin": 1988
                  },
                  {
                    "id": 4,
                    "title": "Brida",
                    "publishedin": 1990
                  }
                ]
              }
            }
            """

        assertEqualsIgnoringOrder(expectedResult, result)
    }

    @Test
    fun `simple query with more depth`() {
        val query = """
            query {
              allAuthors {
                lastName
                books {
                  title
                }
              }
            }
        """

        val ast = parseASTFromQuery(query)
        val trees = buildInternalQueryTree(ast)
        val result = trees.map { tree ->
            executeJooqQuery { ctx ->
                ctx.select(resolveInternalQueryTree(tree)).fetch()
            }
        }.formatGraphQLResponse()

        val expectedResult = """
            {
              "data": {
                "allAuthors": [
                  {
                    "lastName": "Orwell",
                    "books": [
                      { 
                        "title": "1984"
                      },
                      {
                        "title": "Animal Farm"
                      }
                    ]
                  },
                  {
                    "lastName": "Coelho",
                    "books": [
                      {
                        "title": "O Alquimista"
                      },
                      {
                        "title": "Brida"
                      }
                    ]
                  }
                ]
              }
            }
            """

        assertEqualsIgnoringOrder(expectedResult, result)
    }

    @Test
    fun `query returning object`() {
        val query = """
            query {
              test {
                value
              }
            }  
        """

        val ast = parseASTFromQuery(query)
        val trees = buildInternalQueryTree(ast)
        val result = trees.map { tree ->
            executeJooqQuery { ctx ->
                ctx.select(resolveInternalQueryTree(tree)).fetch()
            }
        }.formatGraphQLResponse()

        val expectedResult = """
            {
              "data": {
                "test": {
                  "value": "test"
                }
              }
            }
            """

        assertEqualsIgnoringOrder(expectedResult, result)
    }

    @Test
    fun `query returning null`() {
        val query = """
            query {
              allOrders {
                order_id
                user {
                  user_id
                }
              }
            }
        """

        val ast = parseASTFromQuery(query)
        val trees = buildInternalQueryTree(ast)
        val result = trees.map { tree ->
            executeJooqQuery { ctx ->
                ctx.select(resolveInternalQueryTree(tree)).fetch()
            }
        }.formatGraphQLResponse()

        val expectedResult = """
            {
              "data": {
                "allOrders": [
                  {
                    "order_id": 1,
                    "user": null
                  },
                  {
                    "order_id": 2,
                    "user": {
                      "user_id": 1
                    }
                  },
                  {
                    "order_id": 3,
                    "user": {
                      "user_id": 1
                    }
                  },
                  {
                    "order_id": 4,
                    "user": {
                      "user_id": 2
                    }
                  }
                ]
              }
            }
            """

        assertEqualsIgnoringOrder(expectedResult, result)
    }

    @Test
    fun `query with self-relation`() {
        /*
               A
             /   \
            B     C
           / \   /
          D   E F

         */
        val query = """
            query {
              allTrees {
                label
                parent {
                  label
                }
                children {
                  label
                }
              }
            }
        """

        val ast = parseASTFromQuery(query)
        val trees = buildInternalQueryTree(ast)
        val result = trees.map { tree ->
            executeJooqQuery { ctx ->
                ctx.select(resolveInternalQueryTree(tree)).fetch()
            }
        }.formatGraphQLResponse()

        val expectedResult = """
            {
              "data": {
                "allTrees": [
                  {
                    "label": "A",
                    "parent": null,
                    "children": [
                      {
                        "label": "B"
                      },
                      {
                        "label": "C"
                      }
                    ]
                  },
                  {
                    "label": "B",
                    "parent": {
                      "label": "A"
                    },
                    "children": [
                      {
                        "label": "D"
                      },
                      {
                        "label": "E"
                      }
                    ]
                  },
                  {
                    "label": "C",
                    "parent": {
                      "label": "A"
                    },
                    "children": [
                      {
                        "label": "F"
                      }
                    ]
                  },
                  {
                    "label": "D",
                    "parent": {
                      "label": "B"
                    },
                    "children": []
                  },
                  {
                    "label": "E",
                    "parent": {
                      "label": "B"
                    },
                    "children": []
                  },
                  {
                    "label": "F",
                    "parent": {
                      "label": "C"
                    },
                    "children": []
                  }
                ]
              }
            }
            """

        assertEqualsIgnoringOrder(expectedResult, result)
    }

    @Test
    fun `query with alias`() {
        val query = """
            query {
              novel: allBooks {
                nid: id
                id
                writer: author{
                  id: id
                }
              }
            }
        """

        val ast = parseASTFromQuery(query)
        val trees = buildInternalQueryTree(ast)
        val result = trees.map { tree ->
            executeJooqQuery { ctx ->
                ctx.select(resolveInternalQueryTree(tree)).fetch()
            }
        }.formatGraphQLResponse()

        val expectedResult = """
            {
              "data": {
                "novel": [
                  {
                    "nid": 1,
                    "id": 1,
                    "writer": {
                      "id": 1
                    }
                  },
                  {
                    "nid": 2,
                    "id": 2,
                    "writer": {
                      "id": 1
                    }
                  },
                  {
                    "nid": 3,
                    "id": 3,
                    "writer": {
                      "id": 2
                    }
                  },
                  {
                    "nid": 4,
                    "id": 4,
                    "writer": {
                      "id": 2
                    }
                  }
                ]
              }
            }
            """

        assertEqualsIgnoringOrder(expectedResult, result)
    }

    @Test
    fun `multiple queries?`() {
        val query = """
            query {
              test {value}
              test2: test {value}
            }
        """

        val ast = parseASTFromQuery(query)
        val trees = buildInternalQueryTree(ast)
        val result = trees.map { tree ->
            executeJooqQuery { ctx ->
                ctx.select(resolveInternalQueryTree(tree)).fetch()
            }
        }.formatGraphQLResponse()

        val expectedResult = """
            {
              "data": {
                "test": {
                  "value": "test"
                },
                "test2": {
                  "value": "test"
                }
              }
            }
            """

        assertEqualsIgnoringOrder(expectedResult, result)
    }

    @Test
    fun `query with argument`() {
        val query = """
            query {
              authorById(id: 1) {
                id
                lastName
              }
            }
        """

        val ast = parseASTFromQuery(query)
        val trees = buildInternalQueryTree(ast)
        val result = trees.map { tree ->
            executeJooqQuery { ctx ->
                ctx.select(resolveInternalQueryTree(tree)).fetch()
            }
        }.formatGraphQLResponse()

        val expectedResult = """
            {
              "data": {
                "authorById": {
                  "id": 1,
                  "lastName": "Orwell"
                }
              }
            }
            """

        assertEqualsIgnoringOrder(expectedResult, result)
    }

    @Test
    fun `n to m relation two ways`() {
        val query = """
            query {
              allBookStores {
                name
                books {
                  title
                }
                b2b {
                  stock
                  book {
                    title
                  }
                }
              }
            }
        """

        val ast = parseASTFromQuery(query)
        val trees = buildInternalQueryTree(ast)
        val result = trees.map { tree ->
            executeJooqQuery { ctx ->
                ctx.select(resolveInternalQueryTree(tree)).fetch()
            }
        }.formatGraphQLResponse()

        val expectedResult = """
            {
              "data": {
                "allBookStores": [
                  {
                    "name": "Orell Füssli",
                    "books": [
                      {
                        "title": "1984"
                      },
                      {
                        "title": "Animal Farm"
                      },
                      {
                        "title": "O Alquimista"
                      }
                    ],
                    "b2b": [
                      {
                        "stock": 10,
                        "book": {
                          "title": "1984"
                        }
                      },
                      {
                        "stock": 10,
                        "book": {
                          "title": "Animal Farm"
                        }
                      },
                      {
                        "stock": 10,
                        "book": {
                          "title": "O Alquimista"
                        }
                      }
                    ]
                  },
                  {
                    "name": "Ex Libris",
                    "books": [
                      {
                        "title": "1984"
                      },
                      {
                        "title": "O Alquimista"
                      }
                    ],
                    "b2b": [
                      {
                        "stock": 1,
                        "book": {
                          "title": "1984"
                        }
                      },
                      {
                        "stock": 2,
                        "book": {
                          "title": "O Alquimista"
                        }
                      }
                    ]
                  },
                  {
                    "name": "Buchhandlung im Volkshaus",
                    "books": [
                      {
                        "title": "O Alquimista"
                      }
                    ],
                    "b2b": [
                      {
                        "stock": 1,
                        "book": {
                          "title": "O Alquimista"
                        }
                      }
                    ]
                  }
                ]
              }
            }
        """

        assertEqualsIgnoringOrder(expectedResult, result)
    }
}

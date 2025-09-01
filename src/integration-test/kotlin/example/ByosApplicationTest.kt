package example

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [GraphQLService::class])
internal class ByosApplicationTest {
    @Autowired
    private lateinit var graphQLService: GraphQLService

    @Test
    fun simpleQuery() {
        val query = """
            query {
              allFilms(limit: 3) {
                edges {
                  node {
                    film_id
                    title
                    release_year
                  }
                }
              }
            }
            """.trimIndent()
        val expectedResult = """
            {
              "data": {
                "allFilms": {
                  "edges": [
                    {
                      "node": {
                        "film_id": 1,
                        "title": "ACADEMY DINOSAUR",
                        "release_year": 2006
                      }
                    },
                    {
                      "node": {
                        "film_id": 2,
                        "title": "ACE GOLDFINGER",
                        "release_year": 2006
                      }
                    },
                    {
                      "node": {
                        "film_id": 3,
                        "title": "ADAPTATION HOLES",
                        "release_year": 2006
                      }
                    }
                  ]
                }
              }
            }
        """.trimIndent()
        assertJsonEquals(expectedResult, graphQLService.executeGraphQLQuery(query))
    }

    @Test
    fun simpleQueryWithMoreDepth() {
        val query = """
            query {
              allFilms(limit: 2) {
                edges {
                  node {
                    title
                    actors {
                      actor_id
                    }
                  }
                }
              }
            }
            """.trimIndent()
        val expectedResult = """
            {
              "data": {
                "allFilms": {
                  "edges": [
                    {
                      "node": {
                        "title": "ACADEMY DINOSAUR",
                        "actors": [
                          {
                            "actor_id": 1
                          },
                          {
                            "actor_id": 10
                          },
                          {
                            "actor_id": 20
                          },
                          {
                            "actor_id": 30
                          },
                          {
                            "actor_id": 40
                          },
                          {
                            "actor_id": 53
                          },
                          {
                            "actor_id": 108
                          },
                          {
                            "actor_id": 162
                          },
                          {
                            "actor_id": 188
                          },
                          {
                            "actor_id": 198
                          }
                        ]
                      }
                    },
                    {
                      "node": {
                        "title": "ACE GOLDFINGER",
                        "actors": [
                          {
                            "actor_id": 19
                          },
                          {
                            "actor_id": 85
                          },
                          {
                            "actor_id": 90
                          },
                          {
                            "actor_id": 160
                          }
                        ]
                      }
                    }
                  ]
                }
              }
            }
        """.trimIndent()
        assertJsonEquals(expectedResult, graphQLService.executeGraphQLQuery(query))
    }

    @Test
    fun queryReturningObject() {
        val query = """
            query {
              filmById(where: {film_id: {_eq: 1}}) {
                title
              }
            }

            """.trimIndent()
        val expectedResult = """
            {
              "data": {
                "filmById": {
                  "title": "ACADEMY DINOSAUR"
                }
              }
            }

            """.trimIndent()
        assertJsonEquals(expectedResult, graphQLService.executeGraphQLQuery(query))
    }

    @Test
    fun queryReturningObjects() {
        val query = """
            query {
              filmByIds(where: {film_id: {_in: [1,2]}}) {
                edges {
                  node {
                    title
                  }
                }
              }
            }

            """.trimIndent()
        val expectedResult = """
            {
              "data": {
                "filmByIds": {
                    "edges": [
                        {"node":{"title": "ACADEMY DINOSAUR"}},
                        {"node":{"title": "ACE GOLDFINGER"}}
                    ]
                }
              }
            }

            """.trimIndent()
        assertJsonEquals(expectedResult, graphQLService.executeGraphQLQuery(query))
    }

    @Test
    fun queryReturningNull() {
        val query = """
            query {
              filmById(where: {film_id: {_eq: 1}}) {
                original_language {
                  name
                }
              }
            }
            """.trimIndent()
        val expectedResult = """
            {
              "data": {
                "filmById": {
                  "original_language": null
                }
              }
            }
            """.trimIndent()
        assertJsonEquals(expectedResult, graphQLService.executeGraphQLQuery(query))
    }

    @Test
    fun queryWithSelfRelation() {
        val query = """
            {
              allCategories(limit: 3) {
                edges {
                  node {
                    name
                    parent_category {
                      name
                    }
                    subcategories {
                      edges {
                        node {
                          name
                        }
                      }
                    }
                  }
                }
              }
            }
            """.trimIndent()
        val expectedResult = """
            {
              "data": {
                "allCategories": {
                  "edges": [
                    {
                      "node": {
                        "name": "Action",
                        "parent_category": null,
                        "subcategories": {
                          "edges": []
                        }
                      }
                    },
                    {
                      "node": {
                        "name": "Animation",
                        "parent_category": null,
                        "subcategories": {
                          "edges": []
                        }
                      }
                    },
                    {
                      "node": {
                        "name": "Children",
                        "parent_category": null,
                        "subcategories": {
                          "edges": []
                        }
                      }
                    }
                  ]
                }
              }
            }
            """.trimIndent()
        assertJsonEquals(expectedResult, graphQLService.executeGraphQLQuery(query))
    }

    @Test
    fun queryWithAlias() {
        val query = """
            query {
              movie2: filmById(where: {film_id: {_eq: 2}}) {
                id: film_id
                film_id
                star: actors {
                  aid: actor_id
                }
              }
            }
            """.trimIndent()
        val expectedResult = """
            {
              "data": {
                "movie2": {
                  "id": 2,
                  "film_id": 2,
                  "star": [
                    {
                      "aid": 19
                    },
                    {
                      "aid": 85
                    },
                    {
                      "aid": 90
                    },
                    {
                      "aid": 160
                    }
                  ]
                }
              }
            }
        """.trimIndent()
        assertJsonEquals(expectedResult, graphQLService.executeGraphQLQuery(query))
    }

    @Test
    fun multipleQueries() {
        val query = """
            {
              a1: actorById(where: {actor_id: {_eq: 1}}) {
                last_name
              }
              a2: actorById(where: {actor_id: {_eq: 2}}) {
                last_name
              }
            }
            """.trimIndent()
        val expectedResult = """
            {
              "data": {
                "a1": {
                  "last_name": "GUINESS"
                },
                "a2": {
                  "last_name": "WAHLBERG"
                }
              }
            }
            """.trimIndent()
        assertJsonEquals(expectedResult, graphQLService.executeGraphQLQuery(query))
    }

    @Test
    fun queryWithArgument() {
        val query = """
            query {
              actorById(where: {actor_id: {_eq: 1}}) {
                actor_id
                last_name
              }
            }
            
            """.trimIndent()
        val expectedResult = """
            {
              "data": {
                "actorById": {
                  "actor_id": 1,
                  "last_name": "GUINESS"
                }
              }
            }
            """.trimIndent()
        assertJsonEquals(expectedResult, graphQLService.executeGraphQLQuery(query))
    }

    @Test
    fun nToMRelationTwoWays() {
        val query = """
            {
              allStores {
                edges {
                  node {
                    store_id
                    films(limit: 2) {
                      edges {
                        node {
                          film_id
                        }
                      }
                    }
                    inventories(limit: 2) {
                      edges {
                        node {
                          film {
                            film_id
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
            """.trimIndent()
        val expectedResult = """
            {
              "data": {
                "allStores": {
                  "edges": [
                    {
                      "node": {
                        "store_id": 1,
                        "films": {
                          "edges": [
                            {
                              "node": {
                                "film_id": 1
                              }
                            },
                            {
                              "node": {
                                "film_id": 4
                              }
                            }
                          ]
                        },
                        "inventories": {
                          "edges": [
                            {
                              "node": {
                                "film": {
                                  "film_id": 1
                                }
                              }
                            },
                            {
                              "node": {
                                "film": {
                                  "film_id": 1
                                }
                              }
                            }
                          ]
                        }
                      }
                    },
                    {
                      "node": {
                        "store_id": 2,
                        "films": {
                          "edges": [
                            {
                              "node": {
                                "film_id": 1
                              }
                            },
                            {
                              "node": {
                                "film_id": 2
                              }
                            }
                          ]
                        },
                        "inventories": {
                          "edges": [
                            {
                              "node": {
                                "film": {
                                  "film_id": 1
                                }
                              }
                            },
                            {
                              "node": {
                                "film": {
                                  "film_id": 1
                                }
                              }
                            }
                          ]
                        }
                      }
                    }
                  ]
                }
              }
            }
        """.trimIndent()
        assertJsonEquals(expectedResult, graphQLService.executeGraphQLQuery(query))
    }

    @Test
    fun queryWithFirstLimit() {
        val query = """
            query {
              allFilms(limit: 1) {
                edges {
                  node {
                    film_id
                    actors {
                    actor_id
                      films(release_year: 2006, limit: 1) {
                        edges {
                          node {
                            film_id
                          }
                        }
                      }
                    }
                  }
                  cursor
                }
                totalCount
              }
            }
            """.trimIndent()
        val expectedResult = """
            {
              "data": {
                "allFilms": {
                  "edges": [
                    {
                      "node": {
                        "film_id": 1,
                        "actors": [
                          {
                            "actor_id": 1,
                            "films": {
                              "edges": [
                                {
                                  "node": {
                                    "film_id": 1
                                  }
                                }
                              ]
                            }
                          },
                          {
                            "actor_id": 10,
                            "films": {
                              "edges": [
                                {
                                  "node": {
                                    "film_id": 1
                                  }
                                }
                              ]
                            }
                          },
                          {
                            "actor_id": 20,
                            "films": {
                              "edges": [
                                {
                                  "node": {
                                    "film_id": 1
                                  }
                                }
                              ]
                            }
                          },
                          {
                            "actor_id": 30,
                            "films": {
                              "edges": [
                                {
                                  "node": {
                                    "film_id": 1
                                  }
                                }
                              ]
                            }
                          },
                          {
                            "actor_id": 40,
                            "films": {
                              "edges": [
                                {
                                  "node": {
                                    "film_id": 1
                                  }
                                }
                              ]
                            }
                          },
                          {
                            "actor_id": 53,
                            "films": {
                              "edges": [
                                {
                                  "node": {
                                    "film_id": 1
                                  }
                                }
                              ]
                            }
                          },
                          {
                            "actor_id": 108,
                            "films": {
                              "edges": [
                                {
                                  "node": {
                                    "film_id": 1
                                  }
                                }
                              ]
                            }
                          },
                          {
                            "actor_id": 162,
                            "films": {
                              "edges": [
                                {
                                  "node": {
                                    "film_id": 1
                                  }
                                }
                              ]
                            }
                          },
                          {
                            "actor_id": 188,
                            "films": {
                              "edges": [
                                {
                                  "node": {
                                    "film_id": 1
                                  }
                                }
                              ]
                            }
                          },
                          {
                            "actor_id": 198,
                            "films": {
                              "edges": [
                                {
                                  "node": {
                                    "film_id": 1
                                  }
                                }
                              ]
                            }
                          }
                        ]
                      },
                      "cursor": "{\"film_id\" : 1}"
                    }
                  ],
                  "totalCount": 1000
                }
              }
            }
            """.trimIndent()
        assertJsonEquals(expectedResult, graphQLService.executeGraphQLQuery(query))
    }

    @Test
    fun queryWithCustomOrder() {
        val query = """
            {
              allActors(orderBy: [{last_name: asc}]) {
                actor_id
                last_name
              }
            }
            """.trimIndent()
        val expectedResult = """
            {"data":{"allActors":[{"actor_id":58,"last_name":"AKROYD"},{"actor_id":92,"last_name":"AKROYD"},{"actor_id":182,"last_name":"AKROYD"},{"actor_id":118,"last_name":"ALLEN"},{"actor_id":145,"last_name":"ALLEN"},{"actor_id":194,"last_name":"ALLEN"},{"actor_id":76,"last_name":"ASTAIRE"},{"actor_id":112,"last_name":"BACALL"},{"actor_id":67,"last_name":"BAILEY"},{"actor_id":190,"last_name":"BAILEY"},{"actor_id":115,"last_name":"BALE"},{"actor_id":187,"last_name":"BALL"},{"actor_id":47,"last_name":"BARRYMORE"},{"actor_id":158,"last_name":"BASINGER"},{"actor_id":124,"last_name":"BENING"},{"actor_id":174,"last_name":"BENING"},{"actor_id":14,"last_name":"BERGEN"},{"actor_id":121,"last_name":"BERGMAN"},{"actor_id":12,"last_name":"BERRY"},{"actor_id":60,"last_name":"BERRY"},{"actor_id":91,"last_name":"BERRY"},{"actor_id":189,"last_name":"BIRCH"},{"actor_id":25,"last_name":"BLOOM"},{"actor_id":37,"last_name":"BOLGER"},{"actor_id":185,"last_name":"BOLGER"},{"actor_id":98,"last_name":"BRIDGES"},{"actor_id":39,"last_name":"BRODY"},{"actor_id":159,"last_name":"BRODY"},{"actor_id":167,"last_name":"BULLOCK"},{"actor_id":11,"last_name":"CAGE"},{"actor_id":40,"last_name":"CAGE"},{"actor_id":181,"last_name":"CARREY"},{"actor_id":86,"last_name":"CHAPLIN"},{"actor_id":3,"last_name":"CHASE"},{"actor_id":176,"last_name":"CHASE"},{"actor_id":183,"last_name":"CLOSE"},{"actor_id":16,"last_name":"COSTNER"},{"actor_id":26,"last_name":"CRAWFORD"},{"actor_id":129,"last_name":"CRAWFORD"},{"actor_id":49,"last_name":"CRONYN"},{"actor_id":104,"last_name":"CRONYN"},{"actor_id":105,"last_name":"CROWE"},{"actor_id":57,"last_name":"CRUISE"},{"actor_id":80,"last_name":"CRUZ"},{"actor_id":81,"last_name":"DAMON"},{"actor_id":4,"last_name":"DAVIS"},{"actor_id":101,"last_name":"DAVIS"},{"actor_id":110,"last_name":"DAVIS"},{"actor_id":48,"last_name":"DAY-LEWIS"},{"actor_id":35,"last_name":"DEAN"},{"actor_id":143,"last_name":"DEAN"},{"actor_id":138,"last_name":"DEE"},{"actor_id":148,"last_name":"DEE"},{"actor_id":41,"last_name":"DEGENERES"},{"actor_id":107,"last_name":"DEGENERES"},{"actor_id":166,"last_name":"DEGENERES"},{"actor_id":89,"last_name":"DENCH"},{"actor_id":123,"last_name":"DENCH"},{"actor_id":100,"last_name":"DEPP"},{"actor_id":160,"last_name":"DEPP"},{"actor_id":109,"last_name":"DERN"},{"actor_id":173,"last_name":"DREYFUSS"},{"actor_id":36,"last_name":"DUKAKIS"},{"actor_id":188,"last_name":"DUKAKIS"},{"actor_id":106,"last_name":"DUNST"},{"actor_id":19,"last_name":"FAWCETT"},{"actor_id":199,"last_name":"FAWCETT"},{"actor_id":10,"last_name":"GABLE"},{"actor_id":127,"last_name":"GARLAND"},{"actor_id":165,"last_name":"GARLAND"},{"actor_id":184,"last_name":"GARLAND"},{"actor_id":154,"last_name":"GIBSON"},{"actor_id":46,"last_name":"GOLDBERG"},{"actor_id":139,"last_name":"GOODING"},{"actor_id":191,"last_name":"GOODING"},{"actor_id":71,"last_name":"GRANT"},{"actor_id":1,"last_name":"GUINESS"},{"actor_id":90,"last_name":"GUINESS"},{"actor_id":179,"last_name":"GUINESS"},{"actor_id":32,"last_name":"HACKMAN"},{"actor_id":175,"last_name":"HACKMAN"},{"actor_id":56,"last_name":"HARRIS"},{"actor_id":141,"last_name":"HARRIS"},{"actor_id":152,"last_name":"HARRIS"},{"actor_id":97,"last_name":"HAWKE"},{"actor_id":151,"last_name":"HESTON"},{"actor_id":28,"last_name":"HOFFMAN"},{"actor_id":79,"last_name":"HOFFMAN"},{"actor_id":169,"last_name":"HOFFMAN"},{"actor_id":161,"last_name":"HOPE"},{"actor_id":50,"last_name":"HOPKINS"},{"actor_id":113,"last_name":"HOPKINS"},{"actor_id":134,"last_name":"HOPKINS"},{"actor_id":132,"last_name":"HOPPER"},{"actor_id":170,"last_name":"HOPPER"},{"actor_id":65,"last_name":"HUDSON"},{"actor_id":52,"last_name":"HUNT"},{"actor_id":140,"last_name":"HURT"},{"actor_id":119,"last_name":"JACKMAN"},{"actor_id":131,"last_name":"JACKMAN"},{"actor_id":8,"last_name":"JOHANSSON"},{"actor_id":64,"last_name":"JOHANSSON"},{"actor_id":146,"last_name":"JOHANSSON"},{"actor_id":82,"last_name":"JOLIE"},{"actor_id":43,"last_name":"JOVOVICH"},{"actor_id":74,"last_name":"KEITEL"},{"actor_id":130,"last_name":"KEITEL"},{"actor_id":198,"last_name":"KEITEL"},{"actor_id":23,"last_name":"KILMER"},{"actor_id":45,"last_name":"KILMER"},{"actor_id":55,"last_name":"KILMER"},{"actor_id":153,"last_name":"KILMER"},{"actor_id":162,"last_name":"KILMER"},{"actor_id":103,"last_name":"LEIGH"},{"actor_id":5,"last_name":"LOLLOBRIGIDA"},{"actor_id":157,"last_name":"MALDEN"},{"actor_id":136,"last_name":"MANSFIELD"},{"actor_id":22,"last_name":"MARX"},{"actor_id":70,"last_name":"MCCONAUGHEY"},{"actor_id":77,"last_name":"MCCONAUGHEY"},{"actor_id":114,"last_name":"MCDORMAND"},{"actor_id":38,"last_name":"MCKELLEN"},{"actor_id":177,"last_name":"MCKELLEN"},{"actor_id":27,"last_name":"MCQUEEN"},{"actor_id":128,"last_name":"MCQUEEN"},{"actor_id":42,"last_name":"MIRANDA"},{"actor_id":120,"last_name":"MONROE"},{"actor_id":178,"last_name":"MONROE"},{"actor_id":7,"last_name":"MOSTEL"},{"actor_id":99,"last_name":"MOSTEL"},{"actor_id":61,"last_name":"NEESON"},{"actor_id":62,"last_name":"NEESON"},{"actor_id":6,"last_name":"NICHOLSON"},{"actor_id":108,"last_name":"NOLTE"},{"actor_id":122,"last_name":"NOLTE"},{"actor_id":125,"last_name":"NOLTE"},{"actor_id":150,"last_name":"NOLTE"},{"actor_id":15,"last_name":"OLIVIER"},{"actor_id":34,"last_name":"OLIVIER"},{"actor_id":21,"last_name":"PALTROW"},{"actor_id":69,"last_name":"PALTROW"},{"actor_id":30,"last_name":"PECK"},{"actor_id":33,"last_name":"PECK"},{"actor_id":87,"last_name":"PECK"},{"actor_id":73,"last_name":"PENN"},{"actor_id":133,"last_name":"PENN"},{"actor_id":88,"last_name":"PESCI"},{"actor_id":171,"last_name":"PFEIFFER"},{"actor_id":51,"last_name":"PHOENIX"},{"actor_id":54,"last_name":"PINKETT"},{"actor_id":84,"last_name":"PITT"},{"actor_id":75,"last_name":"POSEY"},{"actor_id":93,"last_name":"PRESLEY"},{"actor_id":135,"last_name":"REYNOLDS"},{"actor_id":142,"last_name":"RYDER"},{"actor_id":180,"last_name":"SILVERSTONE"},{"actor_id":195,"last_name":"SILVERSTONE"},{"actor_id":78,"last_name":"SINATRA"},{"actor_id":31,"last_name":"SOBIESKI"},{"actor_id":44,"last_name":"STALLONE"},{"actor_id":24,"last_name":"STREEP"},{"actor_id":116,"last_name":"STREEP"},{"actor_id":192,"last_name":"SUVARI"},{"actor_id":9,"last_name":"SWANK"},{"actor_id":66,"last_name":"TANDY"},{"actor_id":155,"last_name":"TANDY"},{"actor_id":59,"last_name":"TAUTOU"},{"actor_id":53,"last_name":"TEMPLE"},{"actor_id":149,"last_name":"TEMPLE"},{"actor_id":193,"last_name":"TEMPLE"},{"actor_id":200,"last_name":"TEMPLE"},{"actor_id":126,"last_name":"TOMEI"},{"actor_id":18,"last_name":"TORN"},{"actor_id":94,"last_name":"TORN"},{"actor_id":102,"last_name":"TORN"},{"actor_id":20,"last_name":"TRACY"},{"actor_id":117,"last_name":"TRACY"},{"actor_id":17,"last_name":"VOIGHT"},{"actor_id":2,"last_name":"WAHLBERG"},{"actor_id":95,"last_name":"WAHLBERG"},{"actor_id":196,"last_name":"WALKEN"},{"actor_id":29,"last_name":"WAYNE"},{"actor_id":163,"last_name":"WEST"},{"actor_id":197,"last_name":"WEST"},{"actor_id":72,"last_name":"WILLIAMS"},{"actor_id":137,"last_name":"WILLIAMS"},{"actor_id":172,"last_name":"WILLIAMS"},{"actor_id":83,"last_name":"WILLIS"},{"actor_id":96,"last_name":"WILLIS"},{"actor_id":164,"last_name":"WILLIS"},{"actor_id":168,"last_name":"WILSON"},{"actor_id":68,"last_name":"WINSLET"},{"actor_id":147,"last_name":"WINSLET"},{"actor_id":144,"last_name":"WITHERSPOON"},{"actor_id":13,"last_name":"WOOD"},{"actor_id":156,"last_name":"WOOD"},{"actor_id":63,"last_name":"WRAY"},{"actor_id":85,"last_name":"ZELLWEGER"},{"actor_id":111,"last_name":"ZELLWEGER"},{"actor_id":186,"last_name":"ZELLWEGER"}]}}
            """.trimIndent()
        assertJsonEquals(expectedResult, graphQLService.executeGraphQLQuery(query))
    }

    @Test
    fun queryWithCustomOrder_with_edges() {
        val query = """
            {
              allFilms(limit: 3, orderBy: [{title: asc}]) {
                edges {
                  node {
                    film_id
                    title
                  }
                  cursor
                }
              }
            }
            """.trimIndent()
        val expectedResult = """
            {
              "data": {
                "allFilms": {
                  "edges": [
                    {
                      "node": {
                        "film_id": 1,
                        "title": "ACADEMY DINOSAUR"
                      },
                      "cursor": "{\"title\" : \"ACADEMY DINOSAUR\", \"film_id\" : 1}"
                    },
                    {
                      "node": {
                        "film_id": 2,
                        "title": "ACE GOLDFINGER"
                      },
                      "cursor": "{\"title\" : \"ACE GOLDFINGER\", \"film_id\" : 2}"
                    },
                    {
                      "node": {
                        "film_id": 3,
                        "title": "ADAPTATION HOLES"
                      },
                      "cursor": "{\"title\" : \"ADAPTATION HOLES\", \"film_id\" : 3}"
                    }
                  ]
                }
              }
            }
            """.trimIndent()
        assertJsonEquals(expectedResult, graphQLService.executeGraphQLQuery(query))
    }

    @Test
    fun orderByMultipleFieldsAndUseCursor() {
        val query = """
            {
              allFilms (orderBy: [{release_year: asc, title: desc, film_id: asc}], after: "{\"release_year\" : 2006, \"title\" : \"AFFAIR PREJUDICE\", \"film_id\" : 4}") {
                edges {
                  node {
                    title
                  }
                  cursor
                }
                totalCount
              }
            }
            """.trimIndent()
        val expectedResult = """
            {
              "data": {
                "allFilms": {
                  "edges": [
                    {
                      "node": {
                        "title": "ADAPTATION HOLES"
                      },
                      "cursor": "{\"release_year\" : 2006, \"title\" : \"ADAPTATION HOLES\", \"film_id\" : 3}"
                    },
                    {
                      "node": {
                        "title": "ACE GOLDFINGER"
                      },
                      "cursor": "{\"release_year\" : 2006, \"title\" : \"ACE GOLDFINGER\", \"film_id\" : 2}"
                    },
                    {
                      "node": {
                        "title": "ACADEMY DINOSAUR"
                      },
                      "cursor": "{\"release_year\" : 2006, \"title\" : \"ACADEMY DINOSAUR\", \"film_id\" : 1}"
                    }
                  ],
                  "totalCount": 1000
                }
              }
            }
            """.trimIndent()
        assertJsonEquals(expectedResult, graphQLService.executeGraphQLQuery(query))
    }

    @Test
    fun queryWithDistinctOn() {
        val query = """
        query {
          allFilms(distinctOn: [title], orderBy: [{title: asc}], limit: 3) {
            edges {
              node {
                title
                release_year
              }
            }
          }
        }
    """.trimIndent()

        val expectedResult = """
        {
          "data": {
            "allFilms": {
              "edges": [
                {
                  "node": {
                    "title": "ACADEMY DINOSAUR",
                    "release_year": 2006
                  }
                },
                {
                  "node": {
                    "title": "ACE GOLDFINGER",
                    "release_year": 2006
                  }
                },
                {
                  "node": {
                    "title": "ADAPTATION HOLES",
                    "release_year": 2006
                  }
                }
              ]
            }
          }
        }
    """.trimIndent()

        assertJsonEquals(expectedResult, graphQLService.executeGraphQLQuery(query))
    }

    @Test
    fun queryWithDistinctOnAndRatingFilter() {
        val query = """
        query {
          allFilms(
            distinctOn: [release_year],
            orderBy: [{ release_year: asc }]
         ) {
            edges {
              node {
                title
                release_year
              }
            }
          }
        }
    """.trimIndent()

        val expectedResult = """
        {
          "data": {
            "allFilms": {
              "edges": [
                {
                  "node": {
                    "title": "ACADEMY DINOSAUR",
                    "release_year": 2006
                  }
                }
              ]
            }
          }
        }
    """.trimIndent()

        assertJsonEquals(expectedResult, graphQLService.executeGraphQLQuery(query))
    }
    @Test
    fun queryWithRecursiveFragments() {
        val query = """
            fragment FilmBaseFields on Film {
              film_id
              title
            }

            fragment FilmWithLimitedActors on Film {
              ...FilmBaseFields
            }
            query RecursiveFragmentTest {
              film1: filmById(where: {film_id: {_eq: 1}}) {
                ...FilmWithLimitedActors
              }
            }
        """.trimIndent()

        val expectedResult = """
            {
              "data": {
                "film1": {
                  "film_id": 1,
                  "title": "ACADEMY DINOSAUR"
                }
              }  
            }    
        
        """.trimIndent()
        assertJsonEquals(expectedResult, graphQLService.executeGraphQLQuery(query))
    }
}

const express = require("express");
const app = express();
const handlebars = require("express-handlebars").engine;
const bodyParser = require("body-parser");
const cookieParser = require("cookie-parser"); // Importar o cookie-parser
const { initializeApp, cert } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getAuth } = require("firebase-admin/auth");
const serviceAccount = require("./firebase/chave-firebase.json");
const axios = require("axios");

// Inicializar Firebase Admin
initializeApp({
  credential: cert(serviceAccount),
});

const db = getFirestore();
const auth = getAuth();

app.engine("handlebars", handlebars({ defaultLayout: "main" }));
app.set("view engine", "handlebars");

app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());
app.use(cookieParser()); // Middleware de cookies
app.use(express.static("public"));

// Middleware para verificar autenticação
function checkAuth(req, res, next) {
  const sessionCookie = req.cookies.session || ""; // Acessar cookies com cookie-parser
  auth
    .verifySessionCookie(sessionCookie, true)
    .then((decodedClaims) => {
      req.user = decodedClaims; // Adiciona os dados do usuário à requisição
      next();
    })
    .catch(() => {
      req.user = null; // Nenhum usuário autenticado
      next();
    });
}

app.get("/", checkAuth, async (req, res) => {
  if (req.user) {
    try {
      // Obter as receitas que não pertencem ao usuário logado
      const recipesSnapshot = await db
        .collection("recipes")
        .where("userId", "!=", req.user.uid) // Filtrar receitas cujo userId é diferente do do usuário logado
        .get();

      const recipes = [];
      recipesSnapshot.forEach((doc) => {
        recipes.push({
          id: doc.id,
          ...doc.data(), // Espalha os campos diretamente no objeto
        });
      });

      // Renderizar a página com as receitas e o status de login
      res.render("index", {
        recipes,
        user: req.user,
      });
    } catch (error) {
      console.error("Erro ao buscar receitas:", error);
      res.status(500).send("Erro ao carregar receitas.");
    }
  } else {
    res.redirect("/login");
  }
});

// Login
app.get("/login", (req, res) => {
  res.render("login");
});

app.post("/login", async (req, res) => {
  const { email, password } = req.body;

  try {
    // Enviar uma solicitação para o Firebase REST API
    const response = await axios.post(
      "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword",
      {
        email,
        password,
        returnSecureToken: true,
      },
      {
        params: {
          key: "AIzaSyCPr9gHnVNJLONMttDSmeT1s50akeRE3SU",
        },
      }
    );

    // Extrair o token ID do usuário autenticado
    const idToken = response.data.idToken;

    // Criar um cookie de sessão a partir do token ID
    const sessionCookie = await auth.createSessionCookie(idToken, {
      expiresIn: 60 * 60 * 24 * 5 * 1000, // 5 dias
    });

    // Configurar o cookie de sessão no cliente
    res.cookie("session", sessionCookie, {
      httpOnly: true,
      secure: true,
    });

    res.redirect("/");
  } catch (error) {
    console.error("Erro ao autenticar usuário:", error.response?.data || error.message);
    res.redirect("/login"); // Volta ao login em caso de erro
  }
});

// Cadastro de novos usuários
app.get("/register", checkAuth, async (req, res) => {
  if (req.user) {
    res.redirect("/")
  } else {
    res.render("register");
  }
});

app.post("/register", async (req, res) => {
  const { nome, email, password } = req.body;

  try {
    const userRecord = await auth.createUser({ email, password });
    console.log("Usuário criado:", userRecord.uid);
    var result = db.collection('usuarios').add({
      nome: nome,
      email: email
    }).then(function () {
      console.log('Usuário Cadastrado!');
      res.redirect('/login')
    })
  } catch (error) {
    console.log("Erro ao criar usuário:", error);
    res.redirect("/register");
  }
});

// Logout
app.get("/logout", (req, res) => {
  res.clearCookie("session");
  res.redirect("/");
});

app.get("/search", async (req, res) => {
  try {
    const query = req.query.q; // Obtém o termo de pesquisa da URL
    const recipesRef = db.collection("recipes");
    const recipesSnapshot = await recipesRef
      .where("title", ">=", query) // Busca que começa com o termo digitado
      .where("title", "<=", query + "\uf8ff") // Limita a busca ao intervalo de prefixo
      .get();

    const recipes = [];
    recipesSnapshot.forEach((doc) => {
      recipes.push({ id: doc.id, ...doc.data() });
    });

    res.json({ recipes });
  } catch (error) {
    console.error("Erro ao buscar receitas:", error);
    res.status(500).json({ error: "Erro ao buscar receitas" });
  }
});

app.get("/view_recipe/:id", checkAuth, async (req, res) => {
  if (req.user) {
    const recipeId = req.params.id;

    try {
      const doc = await db.collection('recipes').doc(recipeId).get();

      if (!doc.exists) {
        return res.status(404).send("Receita não encontrada");
      }

      const recipeData = doc.data();

      // Dividindo os ingredientes pela vírgula
      const ingredients = recipeData.ingredients.split(',').map(ingredient => ingredient.trim());

      // Passando os dados para o template
      res.render("view_recipe", {
        recipe: { id: doc.id, ...recipeData, ingredients }
      });
    } catch (error) {
      console.log("Erro ao recuperar receita:", error);
      res.status(500).send("Erro ao recuperar receita");
    }
  } else {
    res.redirect("/login");
  }
});

app.get("/myrecipes", checkAuth, async (req, res) => {
  if (req.user) {
    try {
      // Obter as receitas que não pertencem ao usuário logado
      const recipesSnapshot = await db
        .collection("recipes")
        .where("userId", "==", req.user.uid) // Filtrar receitas cujo userId é diferente do do usuário logado
        .get();

      const recipes = [];
      recipesSnapshot.forEach((doc) => {
        recipes.push({
          id: doc.id,
          ...doc.data(), // Espalha os campos diretamente no objeto
        });
      });

      // Renderizar a página com as receitas e o status de login
      res.render("myrecipes", {
        recipes,
        user: req.user,
      });
    } catch (error) {
      console.error("Erro ao buscar receitas:", error);
      res.status(500).send("Erro ao carregar receitas.");
    }
  } else {
    res.redirect("/login");
  }
})

app.get("/add_recipe", checkAuth, async (req, res) => {
  res.render("add_recipe")
})

app.post("/add_recipe", checkAuth, async (req, res) => {
  if (req.user) {
    const title = req.body.title;
    const ingredients = req.body.ingredients;
    const instructions = req.body.instructions;

    try {

      const email = req.user.email;

      // Recuperar o usuário do Firestore usando o email
      const userSnapshot = await db.collection("usuarios").where("email", "==", email).get();

      if (userSnapshot.empty) {
        return res.status(400).send("Usuário não encontrado no Firestore.");
      }

      const userDoc = userSnapshot.docs[0];

      // Recuperar o nome (ou outro campo) do usuário
      const username = userDoc.exists ? userDoc.data().nome : "Anônimo"; // Aqui estamos pegando o campo 'name'

      // Gerar um ID para a receita
      const recipeRef = db.collection("recipes").doc();
      const recipeId = recipeRef.id;

      // Adicionar a receita ao Firestore
      await recipeRef.set({
        id: recipeId, // Salvar o ID gerado como um campo no documento
        title: title,
        ingredients: ingredients,
        instructions: instructions,
        userId: req.user.uid, // Associar receita ao usuário logado
        user: username, // Adicionar o nome do usuário
      });

      console.log("Receita adicionada com sucesso!");
      res.redirect("/"); // Redirecionar após o sucesso
    } catch (error) {
      console.error("Erro ao adicionar receita:", error.message);
      res.status(400).send("Erro ao adicionar receita: " + error.message);
    }
  } else {
    res.redirect("/login"); // Redirecionar para login se não autenticado
  }
});


app.get("/edit_recipe/:id", checkAuth, async (req, res) => {
  if (req.user) {
    const recipeId = req.params.id;

    try {
      const doc = await db.collection('recipes').doc(recipeId).get();

      if (!doc.exists) {
        return res.status(404).send("Receita não encontrada");
      }

      const recipeData = doc.data();

      // Dividindo os ingredientes pela vírgula
      const ingredients = recipeData.ingredients.split(',').map(ingredient => ingredient.trim());

      // Passando os dados para o template
      res.render("edit_recipe", {
        recipe: { id: doc.id, ...recipeData, ingredients }
      });
    } catch (error) {
      console.log("Erro ao recuperar receita:", error);
      res.status(500).send("Erro ao recuperar receita");
    }
  } else {
    res.redirect("/login")
  }
})

app.post("/edit_recipe/:id", checkAuth, async (req, res) => {
  if (req.user) {
    const recipeId = req.params.id;
    const { title, ingredients, instructions } = req.body;

    try {
      // Recuperar o documento da receita
      const recipeRef = db.collection('recipes').doc(recipeId);
      const recipeDoc = await recipeRef.get();

      if (!recipeDoc.exists) {
        return res.status(404).send("Receita não encontrada");
      }

      const updateData = {};

      // Adicionar ao objeto de atualização apenas os campos que foram modificados
      if (title) updateData.title = title;
      if (ingredients) updateData.ingredients = ingredients;
      if (instructions) updateData.instructions = instructions;

      // Atualizar o documento com os novos valores
      await recipeRef.update(updateData);

      console.log("Receita atualizada com sucesso!");
      res.redirect("/"); // Redirecionar após o sucesso
    } catch (error) {
      console.error("Erro ao atualizar receita:", error.message);
      res.status(400).send("Erro ao atualizar receita: " + error.message);
    }
  } else {
    res.redirect("/login"); // Redirecionar para login se não autenticado
  }
});

app.get("/manage_recipe/:id", checkAuth, async (req, res) => {
  if (req.user) {
    const recipeId = req.params.id;

    try {
      const doc = await db.collection('recipes').doc(recipeId).get();

      if (!doc.exists) {
        return res.status(404).send("Receita não encontrada");
      }

      const recipeData = doc.data();

      // Dividindo os ingredientes pela vírgula
      const ingredients = recipeData.ingredients.split(',').map(ingredient => ingredient.trim());

      // Passando os dados para o template
      res.render("manage_recipe", {
        recipe: { id: doc.id, ...recipeData, ingredients }
      });
    } catch (error) {
      console.log("Erro ao recuperar receita:", error);
      res.status(500).send("Erro ao recuperar receita");
    }
  } else {
    res.redirect("/login")
  }
})

app.get("/sobre", checkAuth, async (req, res) => {
  if (req.user) {
    res.render("sobre")
  } else {
    res.redirect("/login")
  }
})

app.get("/delete_recipe/:id", checkAuth, async (req, res) => {
  if (req.user) {
    const recipeId = req.params.id;

    try {
      // Referência ao documento da receita
      const recipeRef = db.collection('recipes').doc(recipeId);
      const recipeDoc = await recipeRef.get();

      if (!recipeDoc.exists) {
        return res.status(404).send("Receita não encontrada");
      }

      // Excluir o documento
      await recipeRef.delete();

      console.log("Receita excluída com sucesso!");
      res.redirect("/"); // Redirecionar após a exclusão
    } catch (error) {
      console.error("Erro ao excluir receita:", error.message);
      res.status(400).send("Erro ao excluir receita: " + error.message);
    }
  } else {
    res.redirect("/login"); // Redirecionar para login se não autenticado
  }
});


app.listen(8081, () => {
  console.log("Servidor ativo!");
});

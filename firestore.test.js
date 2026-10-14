const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// 1. Unauthenticated checks
test("Unauthenticated user: cannot read resumes", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("resumes").get());
});

test("Unauthenticated user: cannot write resumes", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("resumes").doc("r1").set({
      userId: ALICE_UID,
      candidateName: "Alice",
      overallScore: 85,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

// 2. Cross-user isolation checks
test("Authenticated user: cannot read another user's resumes", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).collection("resumes").doc("r_bob").set({
      userId: BOB_UID,
      candidateName: "Bob",
      overallScore: 90,
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("resumes").doc("r_bob").get());
});

test("Authenticated user: cannot write into another user's resumes", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("resumes").doc("r_hack").set({
      userId: BOB_UID,
      candidateName: "Bob Impostor",
      overallScore: 90,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

// 3. User can read and write their own data
test("Authenticated user: can create and read own valid resume", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("resumes").doc("r_alice").set({
      userId: ALICE_UID,
      candidateName: "Alice Chouhan",
      overallScore: 88,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("resumes").doc("r_alice").get()
  );
});

test("Authenticated user: can manage own preferences", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("preferences").doc("prefs").set({
      userId: ALICE_UID,
      activeAgentId: "resumax",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("preferences").doc("prefs").get()
  );
});

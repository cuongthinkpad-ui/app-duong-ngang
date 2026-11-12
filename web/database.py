"""Small DB adapter: local SQLite; managed PostgreSQL for durable Cloud Run deployments."""
import contextlib,re,sqlite3

class Row(dict):
    def __getitem__(self,key): return list(self.values())[key] if isinstance(key,int) else super().__getitem__(key)

def row_factory(cursor):
    names=[c.name for c in cursor.description] if cursor.description else []
    return lambda values:Row(zip(names,values))

class Postgres:
    def __init__(self,connection):self.connection=connection
    def execute(self,sql,params=()):
        sql=sql.replace("json_extract(s.options,'$.running')=1","(s.options::jsonb->>'running')='true'")
        if 'INSERT OR IGNORE INTO' in sql:
            sql=sql.replace('INSERT OR IGNORE INTO','INSERT INTO')+' ON CONFLICT DO NOTHING'
        if 'INSERT OR REPLACE INTO subscriptions' in sql:
            sql=sql.replace('INSERT OR REPLACE INTO subscriptions','INSERT INTO subscriptions')+' ON CONFLICT(session) DO UPDATE SET payload=EXCLUDED.payload'
        return self.connection.execute(sql.replace('?','%s'),params)
    def executescript(self,script):
        script=script.replace('INTEGER PRIMARY KEY AUTOINCREMENT','BIGSERIAL PRIMARY KEY')
        script=re.sub(r'\bREAL\b','DOUBLE PRECISION',script)
        script=re.sub(r'\bINTEGER\b','BIGINT',script)
        for statement in script.split(';'):
            if statement.strip():self.connection.execute(statement)

@contextlib.contextmanager
def connect(path,url=''):
    if url:
        import psycopg
        with psycopg.connect(url,row_factory=row_factory,connect_timeout=15) as connection:
            yield Postgres(connection)
    else:
        connection=sqlite3.connect(path,timeout=15)
        connection.row_factory=sqlite3.Row
        connection.execute('PRAGMA journal_mode=WAL');connection.execute('PRAGMA busy_timeout=15000')
        try:
            with connection:yield connection
        finally:connection.close()

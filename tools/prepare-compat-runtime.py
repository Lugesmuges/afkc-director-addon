"""Resolve required dependencies of the selected test mods, including Iris's Sodium pin."""
import json,pathlib,urllib.request,urllib.parse,hashlib
root=pathlib.Path(__file__).resolve().parents[1]
out=root/'.local'/'compat-runtime'
out.mkdir(parents=True,exist_ok=True)
def api(path):
    with urllib.request.urlopen(urllib.request.Request('https://api.modrinth.com/v2/'+path,headers={'User-Agent':'AFKC-Director-Compatibility-Test/0.1'}),timeout=35) as response: return json.load(response)
query=urllib.parse.urlencode({'game_versions':json.dumps(['26.3']),'loaders':json.dumps(['fabric'])})
chosen={}
def resolve(project,version=None):
    if project=='P7dR8mSH': return # Provided by Gradle with pinned version.
    if project in chosen and (version is None or chosen[project]['id']==version):return
    if version:v=api('version/'+version)
    else:
        versions=api('project/'+project+'/version?'+query)
        stable=[v for v in versions if v['version_type']=='release']
        if not versions:
            metadata=api('project/'+project)
            if 'fabric' not in metadata.get('loaders',[]):
                print('Skipping dependency for another loader: '+metadata['slug'],flush=True)
                return
            raise RuntimeError('No 26.3 Fabric release: '+project)
        v=(stable or versions)[0]
    actual=v['project_id']
    if actual in chosen and chosen[actual]['id']==v['id']:return
    chosen[actual]=v
    for dep in v['dependencies']:
        if dep['dependency_type']=='required' and dep['project_id']:resolve(dep['project_id'],dep['version_id'])
for project in ['iris','entityculling','modmenu','freecam','zoomify','do-a-barrel-roll','flashback']:resolve(project)
for v in chosen.values():
    f=next((f for f in v['files'] if f['primary']),v['files'][0])
    p=out/f['filename']
    if not p.exists():
        with urllib.request.urlopen(f['url'],timeout=90) as response:p.write_bytes(response.read())
    assert hashlib.sha512(p.read_bytes()).hexdigest()==f['hashes']['sha512']
    print(f['filename'],flush=True)
(out/'versions.json').write_text(json.dumps(list(chosen.values()),indent=2),encoding='utf-8')

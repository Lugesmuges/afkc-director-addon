"""Read public version metadata; optionally download selected runtime test mods."""
import json, pathlib, urllib.request, urllib.parse, hashlib
root=pathlib.Path(__file__).resolve().parents[1]
out=root/'.local'/'compatibility'
out.mkdir(parents=True,exist_ok=True)
projects=['sodium','iris','entityculling','modmenu','sodium-extra','entitytexturefeatures','3dskinlayers','zoomify','mouse-tweaks','sound-physics-remastered','shulkerboxtooltip','distanthorizons','voxy','nvidium','bobby','clientsort','coolrain','particle-rain','herenthere','simple-voice-chat','do-a-barrel-roll','continuity','freecam','flashback','scribble','bigsignwriter','better-block-entities','gamma-utils','extreme_sound_muffler','axiom','boat-item-view','talkingheads']
download={'sodium','iris','entityculling','modmenu','freecam','zoomify','do-a-barrel-roll','flashback'}
rows=[]
for slug in projects:
    try:
        query=urllib.parse.urlencode({'game_versions':json.dumps(['26.3']),'loaders':json.dumps(['fabric'])})
        request=urllib.request.Request('https://api.modrinth.com/v2/project/'+slug+'/version?'+query,headers={'User-Agent':'AFKC-Director-Compatibility-Research/0.1'})
        with urllib.request.urlopen(request,timeout=30) as response: versions=json.load(response)
        if not versions: rows.append({'project':slug,'status':'no Fabric 26.3 release in API'});continue
        v=versions[0]
        row={'project':slug,'version':v['version_number'],'id':v['id'],'dependencies':v['dependencies'],'status':'metadata only'}
        if slug in download:
            f=next((f for f in v['files'] if f['primary']),v['files'][0])
            destination=out/f['filename']
            if not destination.exists():
                with urllib.request.urlopen(f['url'],timeout=90) as response: destination.write_bytes(response.read())
            assert hashlib.sha512(destination.read_bytes()).hexdigest()==f['hashes']['sha512']
            row.update(file=str(destination),sha512=f['hashes']['sha512'])
        rows.append(row)
        print(slug,v['version_number'],flush=True)
    except Exception as error: rows.append({'project':slug,'error':str(error)})
(out/'inventory.json').write_text(json.dumps(rows,indent=2),encoding='utf-8')

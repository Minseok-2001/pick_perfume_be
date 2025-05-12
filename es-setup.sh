curl -X PUT "localhost:9200/perfumes" -H "Content-Type: application/json" -d @settings.json

curl -X PUT "localhost:9200/perfumes/_mapping" -H "Content-Type: application/json" -d @mapping.json